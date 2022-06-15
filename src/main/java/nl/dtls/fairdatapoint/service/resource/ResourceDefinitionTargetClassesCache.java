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
package nl.dtls.fairdatapoint.service.resource;

import nl.dtls.fairdatapoint.database.mongo.repository.ResourceDefinitionRepository;
import nl.dtls.fairdatapoint.database.mongo.repository.MetadataSchemaRepository;
import nl.dtls.fairdatapoint.entity.resource.ResourceDefinition;
import nl.dtls.fairdatapoint.entity.schema.MetadataSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static nl.dtls.fairdatapoint.config.CacheConfig.RESOURCE_DEFINITION_TARGET_CLASSES_CACHE;

@Service
public class ResourceDefinitionTargetClassesCache {

    @Autowired
    private ConcurrentMapCacheManager cacheManager;

    @Autowired
    private ResourceDefinitionRepository resourceDefinitionRepository;

    @Autowired
    private MetadataSchemaRepository metadataSchemaRepository;

    @PostConstruct
    public void computeCache() {
        // Get cache
        Cache cache = cache();

        // Clear cache
        cache.clear();

        // Add to cache
        List<ResourceDefinition> rds = resourceDefinitionRepository.findAll();
        Map<String, MetadataSchema> metadataSchemaMap = new HashMap<>();
        metadataSchemaRepository.findAllByLatestIsTrue().forEach(schema -> {
            if (!metadataSchemaMap.containsKey(schema.getUuid()) || metadataSchemaMap.get(schema.getUuid()).getVersion().compareTo(schema.getVersion()) < 0) {
                metadataSchemaMap.put(schema.getUuid(), schema);
            }
        });
        rds.forEach(rd -> {
            Set<String> targetClassUris = new HashSet<>();
            rd.getMetadataSchemaUuids().forEach(schemaUuid -> {
                if (metadataSchemaMap.containsKey(schemaUuid)) {
                    targetClassUris.addAll(metadataSchemaMap.get(schemaUuid).getTargetClasses());
                    Queue<String> parentUuids = new LinkedList<>(metadataSchemaMap.get(schemaUuid).getExtendSchemas());
                    Set<String> visitedParents = new HashSet<>();
                    String parentUuid = null;
                    while (!parentUuids.isEmpty()) {
                        parentUuid = parentUuids.poll();
                        if (!visitedParents.contains(parentUuid)) {
                            visitedParents.add(parentUuid);
                            targetClassUris.addAll(metadataSchemaMap.get(parentUuid).getTargetClasses());
                            parentUuids.addAll(metadataSchemaMap.get(parentUuid).getExtendSchemas());
                        }
                    }
                }
            });
            cache.put(rd.getUuid(), targetClassUris.stream().toList());
        });
    }

    public List<String> getByUuid(String uuid) {
        return cache().get(uuid, List.class);
    }

    private Cache cache() {
        return cacheManager.getCache(RESOURCE_DEFINITION_TARGET_CLASSES_CACHE);
    }

}
