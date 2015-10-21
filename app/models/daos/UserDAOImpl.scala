package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.{Profile, User}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.dbio.DBIOAction
import scala.concurrent.Future

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import slick.jdbc.meta._
/**
 * Give access to the user object using Slick
 */
class UserDAOImpl() extends UserDAO with DAOSlick {

  import driver.api._

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- slickUsers.filter(_.id === dbUserLoginInfo.userID)
    } yield dbUser
    db.run(userQuery.result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        User(UUID.fromString(user.userID), loginInfo, user.email, Profile(user.fullName, user.age, user.sex), user.avatarURL)
      }
    }
  }

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID) = {
    val query = for {
      dbUser <- slickUsers.filter(_.id === userID.toString)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
    } yield (dbUser, dbLoginInfo)
    db.run(query.result.headOption).map { resultOption =>
      resultOption.map {
        case (user, loginInfo) =>
          User(
            UUID.fromString(user.userID),
            LoginInfo(loginInfo.providerID, loginInfo.providerKey),
            user.email,
            Profile(user.fullName,user.age,user.sex),
            user.avatarURL)
      }
    }
  }

  /**
   * Create a user.
   *
   * @param user The user to create.
   * @return The created user.
   */
  def create(user: User) = {
    val dbUser = DBUser(user.userID.toString, user.email, user.profile.fullName, user.profile.age, user.profile.sex, user.avatarURL)
    val dbLoginInfo = DBLoginInfo(None, user.loginInfo.providerID, user.loginInfo.providerKey)
    // We don't have the LoginInfo id so we try to get it first.
    // If there is no LoginInfo yet for this user we retrieve the id on insertion.    
    val loginInfoAction = {
      val retrieveLoginInfo = slickLoginInfos.filter(
        info => info.providerID === user.loginInfo.providerID &&
          info.providerKey === user.loginInfo.providerKey).result.headOption
      val insertLoginInfo = slickLoginInfos.returning(slickLoginInfos.map(_.id)).
        into((info, id) => info.copy(id = Some(id))) += dbLoginInfo
      for {
        loginInfoOption <- retrieveLoginInfo
        loginInfo <- loginInfoOption.map(DBIO.successful(_)).getOrElse(insertLoginInfo)
      } yield loginInfo
    }
    // combine database actions to be run sequentially
    val actions = (for {
      _ <- slickUsers.insertOrUpdate(dbUser)
      loginInfo <- loginInfoAction
      _ <- slickUserLoginInfos += DBUserLoginInfo(dbUser.userID, loginInfo.id.get)
    } yield ()).transactionally
    // run actions and return user afterwards
    db.run(actions).map(_ => user)
  }

  /**
   * Update a user.
   *
   * @param user The user to update.
   * @return The updated user.
   */
  def update(user:User):Future[User] = {
    val dbUser = DBUser(user.userID.toString, user.email, user.profile.fullName, user.profile.age, user.profile.sex, user.avatarURL)
    db.run(slickUsers.insertOrUpdate(dbUser)).map(_ => user)
  }

  /** new methods**/
  
  /** Create schema. */
  def loadSchema(): Future[Unit] = {
    val schemas = 
    slickUsers.schema ++ slickLoginInfos.schema ++ slickUserLoginInfos.schema ++ slickPasswordInfos.schema    ++ slickOAuth1Infos.schema ++ slickOAuth2Infos.schema  ++ slickOpenIDInfos.schema ++ slickOpenIDAttributes.schema 
    schemas.create.statements.foreach(println)
    db.run(DBIO.seq(schemas.create))
  }
  
  /** Check if db name is present. */
   def checkTable(tableName: String) : Boolean = {
    val action = MTable.getTables
    val future = db.run(action)
    val retVal = future map {result =>
      result map {x => x}
    }
    val x = Await.result(retVal, Duration.Inf).toList.toString.contains(s"MTable(MQName(public.${tableName}_pkey),INDEX,null,None,None,None)")
   
    if (x) {
      true
    } else {
      false
    }
  }

  /** Count all users. */
  def count(): Future[Int] =
    db.run(slickUsers.length.result)


}
