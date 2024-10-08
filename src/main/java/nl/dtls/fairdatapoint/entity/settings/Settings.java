/**
 * The MIT License
 * Copyright © 2017 DTL
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package nl.dtls.fairdatapoint.entity.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Settings {

    private static final List<SettingsMetricsEntry> DEFAULT_METRICS = List.of(
            new SettingsMetricsEntry(
                    "https://purl.org/fair-metrics/FM_F1A",
                    "https://www.ietf.org/rfc/rfc3986.txt"
            ),
            new SettingsMetricsEntry(
                    "https://purl.org/fair-metrics/FM_A1.1",
                    "https://www.wikidata.org/wiki/Q8777"
            )
    );

    @Id
    @JsonIgnore
    private ObjectId id;

    private String appTitle;

    private String appSubtitle;

    private List<SettingsMetricsEntry> metadataMetrics;

    private SettingsPing ping;

    private List<SettingsSearchFilter> searchFilters;

    private SettingsForms forms;

    public static Settings getDefault() {
        return Settings
                .builder()
                .appTitle(null)
                .appSubtitle(null)
                .metadataMetrics(DEFAULT_METRICS)
                .ping(SettingsPing
                        .builder()
                        .enabled(true)
                        .endpoints(Collections.emptyList())
                        .build()
                )
                .searchFilters(Collections.emptyList())
                .forms(SettingsForms
                        .builder()
                        .autocomplete(SettingsFormsAutocomplete
                                .builder()
                                .searchNamespace(true)
                                .sources(Collections.emptyList())
                                .build()
                        )
                        .build()
                )
                .build();
    }

    public List<SettingsSearchFilter> getSearchFilters() {
        if (searchFilters == null) {
            return Collections.emptyList();
        }
        return searchFilters;
    }

    public SettingsForms getForms() {
        if (forms == null) {
            return getDefault().getForms();
        }
        return forms;
    }
}
