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

package models.viewmodels.notificationspage.converters

import base.BaseSpec
import models.notifications.ResponseType._
import models.viewmodels.notificationspage.MovementTotalsResponseType.{EMR, ERS}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import testdata.NotificationTestData.exampleNotificationFrontendModel

class ResponseConverterProviderSpec extends BaseSpec with MockitoSugar {

  private trait Test {
    implicit val messages: Messages = stubMessages()

    val controlResponseConverter: ControlResponseConverter = mock[ControlResponseConverter]
    val ersResponseConverter: ERSResponseConverter = mock[ERSResponseConverter]
    val emrResponseConverter: EMRResponseConverter = mock[EMRResponseConverter]
    val movementResponseConverter: MovementResponseConverter = mock[MovementResponseConverter]
    val unknownResponseConverter: UnknownResponseConverter = mock[UnknownResponseConverter]

    val provider = new ResponseConverterProvider(
      controlResponseConverter,
      ersResponseConverter,
      emrResponseConverter,
      movementResponseConverter,
      unknownResponseConverter
    )
  }

  "ResponseConverterProvider" should {

    "return correct Response Converter" when {

      "provided with MovementResponse" in new Test {

        val input = exampleNotificationFrontendModel(responseType = MovementResponse)

        val converter = provider.provideResponseConverter(input)

        converter mustBe movementResponseConverter
      }

      "provided with ERS MovementTotalsResponse" in new Test {

        val input =
          exampleNotificationFrontendModel(responseType = MovementTotalsResponse, messageCode = ERS.code)

        val converter = provider.provideResponseConverter(input)

        converter mustBe ersResponseConverter
      }

      "provided with EMR MovementTotalsResponse" in new Test {

        val input =
          exampleNotificationFrontendModel(responseType = MovementTotalsResponse, messageCode = EMR.code)

        val converter = provider.provideResponseConverter(input)

        converter mustBe emrResponseConverter
      }

      "provided with ControlResponse" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ControlResponse)

        val converter = provider.provideResponseConverter(input)

        converter mustBe controlResponseConverter
      }
    }

    "return Unknown Response Converter" when {
      "provided with unknown response" in new Test {

        val input = exampleNotificationFrontendModel(responseType = MovementTotalsResponse, messageCode = "UNKNOWN")

        val converter = provider.provideResponseConverter(input)

        converter mustBe unknownResponseConverter
      }
    }
  }

}
