/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.tosca;

import io.vavr.control.Try;
import java.util.function.Function;

/**
 * Helper class providing facilities for migrating from FJ to VAVR
 */
public final class FJToVavrHelper {

    private FJToVavrHelper() {
    }

    public interface Try0 {

        /**
         * Converts a {@link fj.data.Either} to a {@link io.vavr.control.Try}
         *
         * @param e
         * @param onError
         * @return
         */
        static <L, R> Try<L> fromEither(fj.data.Either<L, R> e, Function<R, Throwable> onError) {
            return e.either(Try::success, r -> Try.failure(onError.apply(r)));
        }
    }
}
