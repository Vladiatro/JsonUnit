/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.core.internal;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import net.javacrumbs.jsonunit.core.internal.PathMatcher.AggregatePathMatcher;

import java.util.List;

import static com.jayway.jsonpath.JsonPath.using;
import static java.util.stream.Collectors.toList;

class JsonPathMatcherResolver {

    /**
     * Generates path matcher matching given JsonPath
     */
    static PathMatcher pathMatcherForJsonPath(String jsonPath, Node json) {
        Configuration conf = Configuration.builder()
            .options(Option.AS_PATH_LIST)
            .build();

        List<String> pathList = using(conf).parse(json.getValue()).read(jsonPath);
        return new AggregatePathMatcher(pathList.stream().map(JsonPathMatcherResolver::forPath).collect(toList()));
    }

    private static PathMatcher forPath(String path) {
        return new PathMatcher.SimplePathMatcher(path);
    }
}
