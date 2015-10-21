package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait UserDAO {

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]]

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID): Future[Option[User]]

  /**
   * Create a user.
   *
   * @param user The user to create.
   * @return The created user.
   */
  def create(user: User): Future[User]

  /**
   * Update a user.
   *
   * @param user The user to update.
   * @return The updated user.
   */
  def update(user:User):Future[User]

  /** new methods**/
  def loadSchema() : Future[Unit]
  def checkTable(tableName: String) : Boolean
  def count(): Future[Int]

}
