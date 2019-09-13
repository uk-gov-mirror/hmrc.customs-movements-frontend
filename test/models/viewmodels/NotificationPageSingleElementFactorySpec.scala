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

package models.viewmodels

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import models.UcrBlock
import models.notifications.ResponseType
import models.submissions.{ActionType, SubmissionFrontendModel}
import models.viewmodels.decoder.ErrorCode._
import models.viewmodels.decoder._
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import testdata.CommonTestData._
import testdata.MovementsTestData.exampleSubmissionFrontendModel
import testdata.NotificationTestData.exampleNotificationFrontendModel

class NotificationPageSingleElementFactorySpec extends WordSpec with MustMatchers with MockitoSugar {

  private val testTimestampString = "2019-10-23T12:34+00:00"
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
  private val testTimestamp = ZonedDateTime.parse(testTimestampString, formatter).toInstant

  private val crcCodeKeyFromDecoder = CrcCode.Success
  private val roeKeyFromDecoder = RoeCode.DocumentaryControl
  private val soeKeyFromDecoder = SoeCode.DeclarationAcceptance
  private val AcknowledgedAndProcessedActionCode = ActionCode.AcknowledgedAndProcessed
  private val MucrNotShutConsolidationErrorCode = ErrorCode.MucrNotShutConsolidation

  private trait Test {
    val decoderMock: Decoder = mock[Decoder]
    implicit val messages: Messages = Mockito.spy(stubMessages())
    val factory = new NotificationPageSingleElementFactory(decoderMock)

    when(decoderMock.crc(any[String])).thenReturn(Some(crcCodeKeyFromDecoder))
    when(decoderMock.roe(any[String])).thenReturn(Some(roeKeyFromDecoder))
    when(decoderMock.soe(any[String])).thenReturn(Some(soeKeyFromDecoder))
    when(decoderMock.actionCode(any[String])).thenReturn(Some(AcknowledgedAndProcessedActionCode))
    when(decoderMock.errorCode(any[String])).thenReturn(Some(MucrNotShutConsolidationErrorCode))
  }

  "NotificationPageSingleElementFactory" should {

    "return NotificationsPageSingleElement with values returned by Messages" when {

      "provided with Arrival SubmissionFrontendModel" in new Test {

        val input: SubmissionFrontendModel =
          exampleSubmissionFrontendModel(actionType = ActionType.Arrival, requestTimestamp = testTimestamp)
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.Arrival"),
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.Arrival")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "provided with Departure SubmissionFrontendModel" in new Test {

        val input: SubmissionFrontendModel =
          exampleSubmissionFrontendModel(actionType = ActionType.Departure, requestTimestamp = testTimestamp)
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.Departure"),
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.Departure")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "provided with DucrAssociation SubmissionFrontendModel" in new Test {

        val input: SubmissionFrontendModel = SubmissionFrontendModel(
          eori = validEori,
          conversationId = conversationId,
          actionType = ActionType.DucrAssociation,
          requestTimestamp = testTimestamp,
          ucrBlocks = Seq(
            UcrBlock(ucr = correctUcr, ucrType = "M"),
            UcrBlock(ucr = correctUcr_2, ucrType = "D"),
            UcrBlock(ucr = correctUcr_3, ucrType = "D")
          )
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.DucrAssociation"),
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.DucrAssociation")}</p>" +
              s"<p>$correctUcr_2</p>" +
              s"<p>$correctUcr_3</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "provided with DucrDisassociation SubmissionFrontendModel" in new Test {

        val input: SubmissionFrontendModel = exampleSubmissionFrontendModel(
          actionType = ActionType.DucrDisassociation,
          requestTimestamp = testTimestamp,
          ucr = correctUcr,
          ucrType = "D"
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.DucrDisassociation"),
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.DucrDisassociation")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "provided with ShutMucr SubmissionFrontendModel" in new Test {

        val input: SubmissionFrontendModel = exampleSubmissionFrontendModel(
          actionType = ActionType.ShutMucr,
          requestTimestamp = testTimestamp,
          ucr = correctUcr,
          ucrType = "M"
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.ShutMucr"),
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.ShutMucr")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }
    }

  }

  "NotificationPageSingleElementFactory" when {

    "provided with MovementResponse NotificationFrontendModel" should {

      "call Decoder" in new Test {

        val crcCode = "000"
        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          timestampReceived = testTimestamp,
          crcCode = Some(crcCode)
        )

        factory.build(input)

        verify(decoderMock).crc(meq(crcCode))
      }

      "call Messages passing correct keys and arguments" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000")
        )

        factory.build(input)

        verifyMessagesCalledWith("notifications.elem.title.inventoryLinkingMovementResponse")
        verifyMessagesCalledWith("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34")
        verifyMessagesCalledWith(crcCodeKeyFromDecoder.contentKey)
        verifyMessagesCalledWith("notifications.elem.content.inventoryLinkingMovementResponse.crc")
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementResponse.crc")} ${crcCodeKeyFromDecoder.contentKey}</p>"
          )
        )

        val result: NotificationsPageSingleElement = factory.build(input)

        assertEquality(result, expectedResult)
      }
    }

    "provided with MovementTotalsResponse NotificationFrontendModel" should {

      "call Decoder" in new Test {

        val crcCode = "000"
        val masterRoe = "6"
        val masterSoe = "1"
        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some(crcCode),
          masterRoe = Some(masterRoe),
          masterSoe = Some(masterSoe)
        )

        factory.build(input)

        verify(decoderMock).crc(meq(crcCode))
        verify(decoderMock).roe(meq(masterRoe))
        verify(decoderMock).soe(meq(masterSoe))
      }

      "call Messages passing correct keys and arguments" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000"),
          masterRoe = Some("6"),
          masterSoe = Some("1")
        )

        factory.build(input)

        verifyMessagesCalledWith("notifications.elem.title.inventoryLinkingMovementTotalsResponse")
        verifyMessagesCalledWith("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34")

        verifyMessagesCalledWith(crcCodeKeyFromDecoder.contentKey)
        verifyMessagesCalledWith(roeKeyFromDecoder.contentKey)
        verifyMessagesCalledWith(soeKeyFromDecoder.contentKey)

        verifyMessagesCalledWith("notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc")
        verifyMessagesCalledWith("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")
        verifyMessagesCalledWith("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000"),
          masterRoe = Some("6"),
          masterSoe = Some("1")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc")} ${crcCodeKeyFromDecoder.contentKey}</p>" +
              s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")} ${roeKeyFromDecoder.contentKey}</p>" +
              s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")} ${soeKeyFromDecoder.contentKey}</p>"
          )
        )

        val result: NotificationsPageSingleElement = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "return NotificationsPageSingleElement with values returned by Messages for incomplete input" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000"),
          masterSoe = Some("1")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc")} ${crcCodeKeyFromDecoder.contentKey}</p>" +
              s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")} ${soeKeyFromDecoder.contentKey}</p>"
          )
        )

        val result: NotificationsPageSingleElement = factory.build(input)

        assertEquality(result, expectedResult)
      }
    }

    "provided with ControlResponse NotificationFrontendModel without errors" should {

      "not call Decoder" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(AcknowledgedAndProcessedActionCode.code)
        )

        factory.build(input)

        verify(decoderMock, times(0)).errorCode(any())
      }

      "call Messages passing correct keys and arguments" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(AcknowledgedAndProcessedActionCode.code)
        )

        factory.build(input)

        verifyMessagesCalledWith("notifications.elem.title.inventoryLinkingControlResponse")
        verifyMessagesCalledWith("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34")
        verifyMessagesCalledWith(AcknowledgedAndProcessedActionCode.contentKey)
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(AcknowledgedAndProcessedActionCode.code)
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(s"<p>${messages(AcknowledgedAndProcessedActionCode.contentKey)}</p>")
        )

        val result: NotificationsPageSingleElement = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "return NotificationsPageSingleElement with empty error explanation" in new Test {}
    }

    "provided with ControlResponse NotificationFrontendModel with errors" should {

      "call Decoder" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(AcknowledgedAndProcessedActionCode.code),
          errorCodes = Seq("01", "29", "13")
        )

        factory.build(input)

        verify(decoderMock).errorCode(meq("01"))
        verify(decoderMock).errorCode(meq("29"))
        verify(decoderMock).errorCode(meq("13"))
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        when(decoderMock.errorCode(meq("01"))).thenReturn(Some(InvalidUcrFormat))
        when(decoderMock.errorCode(meq("13"))).thenReturn(Some(NoPriorArrivalFoundAtDepartureLocation))
        when(decoderMock.errorCode(meq("29"))).thenReturn(Some(MucrAlreadyDeparted))

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(AcknowledgedAndProcessedActionCode.code),
          errorCodes = Seq("01", "29", "13")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages(AcknowledgedAndProcessedActionCode.contentKey)}</p>" +
              s"<br/>" +
              s"<p>${messages("decoder.errorCode.InvalidUcrFormat")}</p>" +
              s"<p>${messages("decoder.errorCode.MucrAlreadyDeparted")}</p>" +
              s"<p>${messages("decoder.errorCode.NoPriorArrivalFoundAtDepartureLocation")}</p>"
          )
        )

        val result: NotificationsPageSingleElement = factory.build(input)

        assertEquality(result, expectedResult)
      }
    }

    "provided with ControlResponse NotificationFrontendModel with unknown error" should {

      "return NotificationsPageSingleElement with empty content for this error" in new Test {

        when(decoderMock.errorCode(any[String])).thenReturn(None)
        when(decoderMock.errorCode(meq("01"))).thenReturn(Some(InvalidUcrFormat))
        when(decoderMock.errorCode(meq("13"))).thenReturn(Some(NoPriorArrivalFoundAtDepartureLocation))

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(AcknowledgedAndProcessedActionCode.code),
          errorCodes = Seq("01", "123", "13")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages(AcknowledgedAndProcessedActionCode.contentKey)}</p>" +
              s"<br/>" +
              s"<p>${messages("decoder.errorCode.InvalidUcrFormat")}</p>" +
              s"<p>${messages("decoder.errorCode.NoPriorArrivalFoundAtDepartureLocation")}</p>"
          )
        )

        val result: NotificationsPageSingleElement = factory.build(input)

        assertEquality(result, expectedResult)
      }
    }
  }

  private def assertEquality(actual: NotificationsPageSingleElement, expected: NotificationsPageSingleElement): Unit = {
    actual.title must equal(expected.title)
    actual.timestampInfo must equal(expected.timestampInfo)
    actual.content must equal(expected.content)
  }

  private def verifyMessagesCalledWith(key: String, args: String*)(implicit messages: Messages): Unit =
    verify(messages).apply(meq(key), meq(args))

}
