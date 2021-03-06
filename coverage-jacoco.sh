#!/bin/bash


#
# Copyright (c) 2021.
#
# This file is part of SANE Web of Things Servient.
#
# SANE Web of Things Servient is free software: you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation, either version 3 of the License,
# or (at your option) any later version.
#
# SANE Web of Things Servient is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with SANE Web of Things Servient.  If not, see
# <http://www.gnu.org/licenses/>.
#
inputs=""
for D in *; do
    if [ -d "${D}" ]; then
        jac="$D/target/site/jacoco/jacoco.csv"
        if [ -f "$jac" ]; then
            inputs="$inputs $jac"
        fi
    fi
done

# sum csv values and output as:
# 10 / 20  instructions covered
# 50,00 % covered
awk -F"," '{ instructions += $4 + $5; covered += $5 } END { print covered, "/", instructions, " instructions covered"; print 100*covered/instructions, "% covered" }' $inputs
