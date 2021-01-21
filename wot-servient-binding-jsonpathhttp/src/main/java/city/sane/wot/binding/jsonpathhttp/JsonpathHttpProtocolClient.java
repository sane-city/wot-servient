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
package city.sane.wot.binding.jsonpathhttp;

import city.sane.wot.binding.http.HttpProtocolClient;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.form.Form;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Allows consuming Things via HTTP. The return values can be manipulated with JSON queries.
 */
public class JsonpathHttpProtocolClient extends HttpProtocolClient {
    private static final Logger log = LoggerFactory.getLogger(JsonpathHttpProtocolClient.class);

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        // create form with http protocol href
        Form httpForm = new Form.Builder(form)
                .setHref(form.getHref().replace("jsonpath+http", "http"))
                .build();

        return super.readResource(httpForm).thenApply(content -> {
            if (form.getOptional("sane:jsonPath") != null) {
                try {
                    // apply jsonpath on content
                    String json = new String(content.getBody());

                    String jsonPath = form.getOptional("sane:jsonPath").toString();
                    Object newValue = JsonPath.read(json, jsonPath);

                    return ContentManager.valueToContent(newValue, "application/json");
                }
                catch (InvalidPathException | ContentCodecException e) {
                    log.warn("Unable apply JSON Path", e);
                    return content;
                }
            }
            else {
                log.warn("No JSON Path found in form '{}'", form);
                return content;
            }
        });
    }
}
