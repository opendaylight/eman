/*
 * Copyright © 2015 Copyright (c) Pajarito Technologies LLC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.eman.impl;

import java.util.HashMap;
import java.util.Map;

public class OIDMap {
    HashMap<String, String> OIDs = new HashMap<String, String>();

    public  OIDMap() {
        OIDs.put("eoPower", ".1.3.6.1.2.1.229.1.2.1.1.2");
        OIDs.put("eoPowerUnitMultiplier", ".1.3.6.1.2.1.229.1.2.1.3.2");
        OIDs.put("eoPowerAccuracy", ".1.3.6.1.2.1.229.1.2.1.4.2");
        OIDs.put("eoPowerStateIndex", ".1.3.6.1.2.1.229.1.3.1.1");
    }


    public String getOID(String resource) {
        return OIDs.get(resource);
    }
}