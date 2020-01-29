def thing = [
        id        : 'KlimabotschafterWetterstation',
        title     : 'KlimabotschafterWetterstation',
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

def exposedThing = wot.produce(thing)

exposedThing.addAction("poisonPill", { -> ((String)null).toString()})

exposedThing.expose()