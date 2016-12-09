package org.openrdf.rio.jsonld;

import org.openrdf.rio.RioSetting;
import org.openrdf.rio.helpers.RioSettingImpl;

import java.util.Map;

/**
 * Created by pduchesne on 29/07/14.
 */
public class SesameJSONLDSettings {

    public static final RioSetting<Map> CONTEXT = new RioSettingImpl<Map>(
            "org.openrdf.rio.jsonld.context", "JSONLD Context", null);

    public static final RioSetting<Map> FRAME = new RioSettingImpl<Map>(
            "org.openrdf.rio.jsonld.frame", "JSONLD Frame", null);

}
