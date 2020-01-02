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

package controllers

import controllers.actions.{AuthAction, JourneyRefiner}
import forms.{ArrivalDetails, DepartureDetails, MovementDetails}
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.{ArrivalAnswers, Cache, DepartureAnswers, JourneyType}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import play.twirl.api.Html
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{arrival_details, departure_details}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MovementDetailsController @Inject()(
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  cache: CacheRepository,
  mcc: MessagesControllerComponents,
  details: MovementDetails,
  arrivalDetailsPage: arrival_details,
  departureDetailsPage: departure_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ARRIVE, JourneyType.DEPART)) { implicit request =>
    request.answers match {
      case arrivalAnswers: ArrivalAnswers     => Ok(arrivalPage(arrivalAnswers))
      case departureAnswers: DepartureAnswers => Ok(departurePage(departureAnswers))
    }
  }

  private def arrivalPage(arrivalAnswers: ArrivalAnswers)(implicit request: JourneyRequest[AnyContent]): Html =
    arrivalDetailsPage(arrivalAnswers.arrivalDetails.fold(details.arrivalForm())(details.arrivalForm().fill(_)), arrivalAnswers.consignmentReferences)

  private def departurePage(departureAnswers: DepartureAnswers)(implicit request: JourneyRequest[AnyContent]): Html =
    departureDetailsPage(
      departureAnswers.departureDetails.fold(details.departureForm())(details.departureForm().fill(_)),
      departureAnswers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException)
    )

  def saveMovementDetails(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ARRIVE, JourneyType.DEPART)).async {
    implicit request =>
      (request.answers match {
        case arrivalAnswers: ArrivalAnswers     => handleSavingArrival(arrivalAnswers)
        case departureAnswers: DepartureAnswers => handleSavingDeparture(departureAnswers)
      }).flatMap {
        case Left(resultView) => Future.successful(BadRequest(resultView))
        case Right(call)      => Future.successful(Redirect(call))
      }
  }

  private def handleSavingArrival(arrivalAnswers: ArrivalAnswers)(implicit request: JourneyRequest[AnyContent]): Future[Either[Html, Call]] = {
    val boundForm = details.arrivalForm().bindFromRequest()

    boundForm.fold(
      (formWithErrors: Form[ArrivalDetails]) => Future.successful(Left(arrivalDetailsPage(formWithErrors, arrivalAnswers.consignmentReferences))),
      validForm =>
        cache.upsert(Cache(request.eori, arrivalAnswers.copy(arrivalDetails = Some(validForm)))).map { _ =>
          Right(controllers.routes.LocationController.displayPage())
      }
    )
  }

  private def handleSavingDeparture(departureAnswers: DepartureAnswers)(implicit request: JourneyRequest[AnyContent]): Future[Either[Html, Call]] = {
    def consignmentReference = departureAnswers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException)
    details
      .departureForm()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DepartureDetails]) => Future.successful(Left(departureDetailsPage(formWithErrors, consignmentReference))),
        validForm =>
          cache.upsert(Cache(request.eori, departureAnswers.copy(departureDetails = Some(validForm)))).map { _ =>
            Right(controllers.routes.LocationController.displayPage())
        }
      )
  }
}
