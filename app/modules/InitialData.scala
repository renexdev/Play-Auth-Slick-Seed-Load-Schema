package modules

import java.text.SimpleDateFormat
import javax.inject.Inject
import models.daos.UserDAOImpl
import models.{Profile, User}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

import java.util.concurrent.TimeUnit._
import java.util.UUID
import com.mohiva.play.silhouette.api.{ Environment, LoginInfo }
/** Initial set of data to be imported into the sample application. */

private[modules] class InitialData @Inject() (usersDao: UserDAOImpl) {


  import scala.concurrent.duration._
    import play.api.libs.concurrent.Execution.Implicits.defaultContext

 def insertUser(): Unit = {
    println("Silhuette db exists: "+usersDao.checkTable("user"))

    if(!usersDao.checkTable("user")){
        println("Creates user schema")
        val tables = Try(Await.result(usersDao.loadSchema,  Duration.Inf))
    } 
    if(usersDao.checkTable("user")){
        val insertInitialDataFuture = for {
            count <- usersDao.count() if count == 0
            _ <- usersDao.create(InitialData.users)
        } yield ()
        Try(Await.result(insertInitialDataFuture, Duration.Inf))    
    }
  }

 println("Setup database")
  insertUser()
}
private[modules] object InitialData {
  println("InitialData object")
    def users = 
    User(UUID.randomUUID(),loginInfo = LoginInfo("facebook", "user@facebook.com"),Some("test1@gmail.com"),
        models.Profile(
            fullName = Some("Tito Perez"),
            age = Some(24),
            sex = Some("hombre")
        ),None)

}