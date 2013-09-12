#!/bin/bash
jar cf clearnlp-$1.jar com
rsync -avc clearnlp-$1.jar choij@nlp01.nj3.ipsoft.com:/apps/data/clearnlp/lib
