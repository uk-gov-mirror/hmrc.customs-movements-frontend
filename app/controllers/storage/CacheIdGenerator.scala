/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.storage

import models.requests.{AuthenticatedRequest, LegacyJourneyRequest}

object CacheIdGenerator {

  def eoriCacheId()(implicit request: LegacyJourneyRequest[_]): String =
    request.authenticatedRequest.user.eori

  def cacheId()(implicit request: AuthenticatedRequest[_]): String =
    request.user.eori

  def movementCacheId()(implicit request: LegacyJourneyRequest[_]): String =
    s"${request.choice.value}-${request.authenticatedRequest.user.eori}"
}
