/*
 * Copyright 2019 HM Revenue & Customs
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

package base

import com.codahale.metrics.Timer
import connectors.CustomsDeclareExportsMovementsConnector
import metrics.MovementsMetrics
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.Helpers.NO_CONTENT
import services.{CustomsCacheService, SubmissionService}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

object MockFactory extends MockitoSugar {

  private val emptyCacheMap = CacheMap("", Map.empty)

  def buildCustomsCacheServiceMock: CustomsCacheService = {
    val customsCacheServiceMock = mock[CustomsCacheService]

    when(customsCacheServiceMock.fetchAndGetEntry(any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(None))
    when(customsCacheServiceMock.fetch(any())(any(), any())).thenReturn(Future.successful(None))
    when(customsCacheServiceMock.cache(any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(emptyCacheMap))
    when(customsCacheServiceMock.remove(any())(any(), any())).thenReturn(Future.successful(HttpResponse(NO_CONTENT)))

    customsCacheServiceMock
  }

  def buildSubmissionServiceMock: SubmissionService = {
    val submissionServiceMock = mock[SubmissionService]

    when(submissionServiceMock.submitMovementRequest(any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(INTERNAL_SERVER_ERROR))
    when(submissionServiceMock.submitDucrAssociation(any(), any())(any(), any()))
      .thenReturn(Future.successful(INTERNAL_SERVER_ERROR))
    when(submissionServiceMock.submitDucrDisassociation(any())(any(), any()))
      .thenReturn(Future.successful(INTERNAL_SERVER_ERROR))
    when(submissionServiceMock.submitShutMucrRequest(any())(any(), any()))
      .thenReturn(Future.successful(INTERNAL_SERVER_ERROR))

    submissionServiceMock
  }

  def buildCustomsDeclareExportsMovementsConnectorMock: CustomsDeclareExportsMovementsConnector = {
    val customsDeclareExportsMovementsConnector = mock[CustomsDeclareExportsMovementsConnector]

    when(customsDeclareExportsMovementsConnector.sendArrivalDeclaration(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(NO_CONTENT)))
    when(customsDeclareExportsMovementsConnector.sendDepartureDeclaration(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(NO_CONTENT)))
    when(customsDeclareExportsMovementsConnector.sendAssociationRequest(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(NO_CONTENT)))
    when(customsDeclareExportsMovementsConnector.sendDisassociationRequest(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(NO_CONTENT)))
    when(customsDeclareExportsMovementsConnector.sendShutMucrRequest(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(NO_CONTENT)))

    customsDeclareExportsMovementsConnector
  }

  def buildMovementsMetricsMock: MovementsMetrics = {
    val movementsMetricsMock = mock[MovementsMetrics]

    when(movementsMetricsMock.startTimer(any())).thenReturn(new Timer().time())

    movementsMetricsMock
  }

}
