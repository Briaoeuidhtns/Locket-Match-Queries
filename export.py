#!/usr/bin/env -S mysql-workbench --quit-when-done --run-script
# -*- coding: utf-8 -*-
from __future__ import division, absolute_import, print_function, unicode_literals
# 2.7 reeeeee


from os import environ, path
import re
import logging
import sys

import grt
from grt.modules import DbMySQLFE, Workbench

logging.basicConfig(format='[%(levelname)s] %(message)s', level=logging.INFO)
log = logging.getLogger(__name__)


def main(source, target, options=()):
    merged = {'OmitSchemas': True}
    merged.update(options)
    options = merged
    log.debug('Using config %s', options)

    log.info('Loading %s', source)
    if not path.isfile(source):
        log.fatal('Model does not exist')
        return -1

    Workbench.openModel(source)
    if grt.root.wb.doc is None:
        log.fatal('Model failed to load')
        return -1

    schemas = grt.root.wb.doc.physicalModels[0].catalog

    log.info('Building initial %s', target)
    DbMySQLFE.generateSQLCreateStatements(schemas, schemas.version, options)
    DbMySQLFE.createScriptForCatalogObjects(target, schemas, options)

    # For some reason 'OmitSchemas' still drops/creates the schema
    # and 'GenerateSchemaDrops' doesn't seem to change anything
    # and I can't figure out how to not output to file
    log.info('Patching %s', target)
    with open(target, 'r') as orig_script:
        script_src = re.sub(r'(create|drop) schema .*?;\s*', '',
                            orig_script.read(),
                            flags=re.DOTALL | re.IGNORECASE)
    log.info('Writing %s', target)
    with open(target, 'w') as new_script:
        new_script.write(script_src)


if __name__ == '__main__':
    sys.exit(
        main(source=environ.get('LOCKET_EXPORT_SOURCE', 'database-model.mwb'),
             target=environ.get('LOCKET_EXPORT_TARGET', 'insertTables.sql')))
