package models.services

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {

  /**
   * Create a user.
   *
   * @param user The user to create.
   * @return The created user.
   */
  def create(user: User): Future[User]

  /**
   * Create the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to create.
   * @return The user for whom the profile was created.
   */
  def create(profile: CommonSocialProfile): Future[User]

  /**
   * update a user.
   *
   * @param user The user to update.
   * @return The updated user.
   */
  def update(user: User): Future[User]

}
