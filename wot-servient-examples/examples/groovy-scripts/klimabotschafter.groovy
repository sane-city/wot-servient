/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
import com.fasterxml.jackson.databind.ObjectMapper

def things = [:]

new Timer().schedule({
    def stations = new ObjectMapper().readValue(new URL('http://data.klimabotschafter.de/weatherdata/JSON_Hamburgnet.json'), Map.class)

    for (station in stations.values()) {
        def st_name = station['st_name']

        def exposedThing = things[st_name]

        if (!exposedThing) {
            // create and expose thing
            def thing = [
                    id        : 'KlimabotschafterWetterstationen:' + st_name,
                    title     : 'KlimabotschafterWetterstationen:' + st_name,
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
                    'Temp_2m',
                    [
                            '@type'             : 'saref:Temperature',
                            description         : 'Temperatur in 2m in Grad Celsisus',
                            'om:unit_of_measure': 'om:degree_Celsius',
                            type                : 'number',
                            readOnly            : true,
                            observable          : true
                    ]
            )

            exposedThing.addProperty(
                    'Upload_time',
                    [
                            description: 'Letzter Upload der Daten in UTC',
                            type       : 'string',
                            readOnly   : true,
                            observable : true
                    ]
            )

            exposedThing.addProperty(
                    'Press_sea',
                    [
                            '@type'    : 'saref:Pressure',
                            description: 'Luftdruck in hPa auf Meeresnivea',
                            type       : 'number',
                            readOnly   : true,
                            observable : true
                    ]
            )

            exposedThing.addProperty(
                    'Wind_gust',
                    [
                            description: 'Stärkste Windböe in m/s der letzten 10 Min',
                            type       : 'number',
                            readOnly   : true,
                            observable : true
                    ]
            )

            exposedThing.addProperty(
                    'Wind_dir',
                    [
                            description: 'Windrichtung in Grad (0-360)',
                            type       : 'number',
                            readOnly   : true,
                            observable : true
                    ]
            )

            exposedThing.addProperty(
                    'Rain_year',
                    [
                            description: 'Jahresniederschlag',
                            type       : 'number',
                            readOnly   : true,
                            observable : true
                    ]
            )

            exposedThing.addProperty(
                    'Rain_month',
                    [
                            description: 'Monatlicher Niederschlag in mm',
                            type       : 'number',
                            readOnly   : true,
                            observable : true
                    ]
            )

            exposedThing.addProperty(
                    'Rain_day',
                    [
                            description: 'Tagesniederschlagsmenge (00-00 Uhr) in mm',
                            type       : 'number',
                            readOnly   : true,
                            observable : true
                    ]
            )

            exposedThing.addProperty(
                    'Rain_rate',
                    [
                            description: 'Aktuelle Regenrate in mm/hr',
                            type       : 'number',
                            readOnly   : true,
                            observable : true
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

            exposedThing.addProperty(
                    'Wind_avg',
                    [
                            description: 'Windgeschwindigkeit (10 Min Mittel) in m/s',
                            type       : 'number',
                            readOnly   : true,
                            observable : true
                    ]
            )

            exposedThing.addProperty(
                    'UV_rad',
                    [
                            description: 'UV-Index (Einheit siehe Wikipedia)',
                            type       : 'number',
                            readOnly   : true,
                            observable : true
                    ]
            )

            exposedThing.addProperty(
                    'Hum_2m',
                    [
                            '@type'    : 'saref:Humidity',
                            description: 'Relative Luftfeuchtigkeit 2 m in %',
                            type       : 'number',
                            readOnly   : true,
                            observable : true
                    ]
            )

            exposedThing.addProperty(
                    'Solar_rad',
                    [
                            description: 'Globalstrahlung in W/m^2',
                            type       : 'number',
                            readOnly   : true,
                            observable : true
                    ]
            )

            exposedThing.addProperty(
                    'latitude',
                    [
                            '@type'   : 'sch:latitude',
                            type      : 'number',
                            readOnly  : true,
                            observable: true
                    ]
            )

            things[st_name] = exposedThing

            exposedThing.expose()
        }

        // set property values
        [
                'Temp_2m', 'Upload_time', 'Press_sea', 'Wind_gust', 'Wind_dir', 'Rain_year', 'Rain_month', 'Rain_day', 'Rain_rate', 'longitude', 'Wind_avg',
                'UV_rad', 'Hum_2m', 'Solar_rad', 'latitude'
        ].each {
            exposedThing.properties[it].write(station[it])
        }
    }
}, 0, 60 * 1000)