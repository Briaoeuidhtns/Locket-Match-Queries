#!/usr/bin/env -S mysql-workbench --quit-when-done --run-script
# -*- coding: utf-8 -*-
from __future__ import (absolute_import, division, print_function,
                        unicode_literals)

import logging
import re
import sys
from contextlib import contextmanager
import os
from os import environ, path
from tempfile import NamedTemporaryFile

import grt
from grt.modules import DbMySQLFE, Workbench

log = logging.getLogger(__name__)


@contextmanager
def nullcontext(enter_result=None):
    yield enter_result


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

    with NamedTemporaryFile(mode='r', suffix='.sql') as orig_script:
        log.info('Building initial into %s', orig_script.name)
        DbMySQLFE.generateSQLCreateStatements(
            schemas, schemas.version, options)
        DbMySQLFE.createScriptForCatalogObjects(orig_script.name,
                                                schemas,
                                                options)

        # For some reason 'OmitSchemas' still drops/creates the schema
        # and 'GenerateSchemaDrops' doesn't seem to change anything
        # and I can't figure out how to not output to file
        log.info('Patching script')
        script_src = re.sub(r'(create|drop) schema .*?;\s*', '',
                            orig_script.read(),
                            flags=re.DOTALL | re.IGNORECASE)

        log.info('Writing to %s', 'stdout' if target == '--' else target)
        cm = nullcontext(sys.stdout) if target == '--' else open(target, 'w')
        with cm as new_script:
            new_script.write(script_src)


if __name__ == '__main__':
    # We don't have a seperate stderr when running in mysqlworkbench??
    source = environ.get('LOCKET_EXPORT_SOURCE',
                         path.join(path.dirname(__file__),
                                   'database-model.mwb'))

    target = environ.get('LOCKET_EXPORT_TARGET', 'insertTables.sql')
    quiet = target == '--'
    logfile = source + '.log' if quiet else None
    logging.basicConfig(format='[%(levelname)s] %(message)s',
                        filename=logfile,
                        level=logging.ERROR if quiet else logging.INFO)
    result = main(source=source, target=target)
    if logfile and os.stat(logfile).st_size == 0:
        os.remove(logfile)
    sys.exit(result)
