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

package unit.controllers

import controllers.consolidations.{routes => consolidationRoutes}
import controllers.{routes, ChoiceController}
import forms.Choice
import forms.Choice.AllowedChoiceValues._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.choice_page

import scala.concurrent.ExecutionContext.global

class ChoiceControllerSpec extends ControllerSpec with OptionValues with BeforeAndAfterEach {

  private val mockChoicePage = mock[choice_page]

  private val controller =
    new ChoiceController(mockAuthAction, mockCustomsCacheService, stubMessagesControllerComponents(), mockChoicePage)(
      global
    )

  override def beforeEach {
    super.beforeEach()

    authorizedUser()
    withCaching[Choice](Choice.choiceId, None)
    withCaching(Choice.choiceId)
    when(mockChoicePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach {
    reset(mockChoicePage)

    super.afterEach()
  }

  private def theResponseForm: Form[Choice] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Choice]])
    verify(mockChoicePage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Choice Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in {

        val result = controller.displayChoiceForm()(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "display page method is invoked with data in cache" in {

        val cachedData = Choice(Arrival)
        withCaching(Choice.choiceId, Some(cachedData))

        val result = controller.displayChoiceForm()(getRequest())

        status(result) mustBe OK
        theResponseForm.value.value mustBe cachedData
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        val incorrectForm = Json.toJson(Choice("incorrect"))

        val result = controller.submitChoice()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER)" when {

      "choice is Arrival" in {

        val arrivalForm = JsObject(Map("choice" -> JsString(Arrival)))

        val result = controller.submitChoice()(postRequest(arrivalForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ConsignmentReferencesController.displayPage().url
      }

      "choice is Departure" in {

        val departureForm = JsObject(Map("choice" -> JsString(Departure)))

        val result = controller.submitChoice()(postRequest(departureForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ConsignmentReferencesController.displayPage().url
      }

      "choice is Associate Ducr" in {

        val associateDUCRForm = JsObject(Map("choice" -> JsString(AssociateDUCR)))

        val result = controller.submitChoice()(postRequest(associateDUCRForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe consolidationRoutes.MucrOptionsController.displayPage().url
      }

      "choice is Disassociate Ducr" in {

        val disassociateDUCRForm = JsObject(Map("choice" -> JsString(DisassociateDUCR)))

        val result = controller.submitChoice()(postRequest(disassociateDUCRForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe consolidationRoutes.DisassociateDucrController.displayPage().url
      }

      "choice is Shut Mucr" in {

        val shutMucrForm = JsObject(Map("choice" -> JsString(ShutMucr)))

        val result = controller.submitChoice()(postRequest(shutMucrForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe consolidationRoutes.ShutMucrController.displayPage().url
      }

      "choice is Submission" in {

        val submissionsForm = JsObject(Map("choice" -> JsString(Submissions)))

        val result = controller.submitChoice()(postRequest(submissionsForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.MovementsController.displayPage().url
      }
    }
  }
}
