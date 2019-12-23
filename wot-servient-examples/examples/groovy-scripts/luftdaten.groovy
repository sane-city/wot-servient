import com.fasterxml.jackson.databind.ObjectMapper

import java.util.stream.Collectors
import java.util.stream.StreamSupport

def area = System.getenv('LUFTDATEN_AREA')

if (!area) {
    area = '53.599483,9.933534,0.9'
}

println('Using area: ' + area)

def things = [:]

new Timer().schedule({
    def measurements = new ObjectMapper().readValue(new URL('https://api.luftdaten.info/v1/filter/area=' + area), List.class)

    for (measurement in measurements) {
        def sensor = measurement['sensor']
        def sensorId = sensor['id']
        def sensorDataValues = measurement['sensordatavalues']
        def location = measurement['location']

        def values = StreamSupport.stream(sensorDataValues.spliterator(), false)
                .collect(Collectors.toMap({ v -> v['value_type'] }, { v -> v['value'] }))

        def exposedThing = things[sensorId]

        if (!exposedThing) {
            // create and expose thing
            def thing = [
                    id        : 'luftdaten.info:' + sensorId,
                    title     : 'luftdaten.info:' + sensorId,
                    '@type'   : 'Thing',
                    '@context': [
                            'http://www.w3.org/ns/td',
                            [
                                    om   : 'http://www.wurvoc.org/vocabularies/om-1.8/',
                                    saref: 'https://w3id.org/saref#',
                                    sch  : 'http://schema.org/',
                                    sane : 'https://sane.city/',
                            ]
                    ],
            ]

            exposedThing = wot.produce(thing)

            exposedThing.addProperty(
                    'latitude',
                    [
                            '@type'   : 'sch:latitude',
                            type      : 'number',
                            readOnly  : true,
                            observable: true
                    ]
            )

            exposedThing.addProperty(
                    'longitude',
                    [
                            '@type'   : 'sch:longitude',
                            type      : 'number',
                            readOnly  : true,
                            observable: true
                    ]
            )

            if (values['P1']) {
                exposedThing.addProperty(
                        'P1',
                        [
                                description: 'Feinstaub (PM10) in µg/m³',
                                type       : 'number',
                                readOnly   : true,
                                observable : true
                        ]
                )
            }

            if (values['P2']) {
                exposedThing.addProperty(
                        'P2',
                        [
                                description: 'Feinstaub (PM2,5) in µg/m³',
                                type       : 'number',
                                readOnly   : true,
                                observable : true
                        ]
                )
            }

            if (values['temperature']) {
                exposedThing.addProperty(
                        'temperature',
                        [
                                '@type'             : 'saref:Temperature',
                                'om:unit_of_measure': 'om:degree_Celsius',
                                description         : 'Temperatur in C°',
                                type                : 'number',
                                readOnly            : true,
                                observable          : true
                        ]
                )
            }

            if (values['humidity']) {
                exposedThing.addProperty(
                        'humidity',
                        [
                                '@type'             : 'saref:Humidity',
                                description         : 'Luftfeuchtigkeit in %',
                                type                : 'number',
                                readOnly            : true,
                                observable          : true
                        ]
                )
            }

            if (values['pressure']) {
                exposedThing.addProperty(
                        'pressure',
                        [
                                '@type'             : 'saref:Pressure',
                                type                : 'number',
                                readOnly            : true,
                                observable          : true
                        ]
                )
            }

            if (values['pressure_at_sealevel']) {
                exposedThing.addProperty(
                        'pressure_at_sealevel',
                        [
                                '@type'             : 'saref:Pressure',
                                type                : 'number',
                                readOnly            : true,
                                observable          : true
                        ]
                )
            }

            things[sensorId] = exposedThing

            exposedThing.expose()
        }

        // set property values
        def latitude = location['latitude']
        exposedThing.properties['latitude'].write(latitude)

        def longitude = location['longitude']
        exposedThing.properties['longitude'].write(longitude)

        values.each { name, value ->
            exposedThing.properties[name].write(value)
        }
    }
}, 0, 60 * 1000)