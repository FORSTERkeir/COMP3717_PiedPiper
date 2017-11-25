using System;
using System.Collections;
using System.Data;
using System.Data.SqlClient;
using System.Security.Principal;
using System.Text;
using System.Web;
using GuardianNewWestAPI.Models;
using GuardianNewWestAPI.Utilities;

namespace GuardianNewWestAPI.Filters
{
    public class BasicAuthenticationAttribute : System.Web.Http.Filters.ActionFilterAttribute
    {
        public override void OnActionExecuting(System.Web.Http.Controllers.HttpActionContext actionContext)
        {
            if (actionContext.Request.Headers.Authorization == null)
            {
                actionContext.Response = new System.Net.Http.HttpResponseMessage(System.Net.HttpStatusCode.Unauthorized);
            }
            else
            {
                string authToken = actionContext.Request.Headers.Authorization.Parameter;
                string decodedToken = Encoding.UTF8.GetString(Convert.FromBase64String(authToken));
                string email = decodedToken.Substring(0, decodedToken.IndexOf(":"));
                string password = decodedToken.Substring(decodedToken.IndexOf(":") + 1);

                User user = new User();

                try
                {
                    using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                    {
                        con.Open();
                        using (SqlCommand cmd = con.CreateCommand())
                        {
                            ArrayList colums = new ArrayList();
                            ArrayList conditions = new ArrayList();
                            string statement = string.Empty;

                            colums.Add(User.COL_EMAIL);
                            colums.Add(User.COL_PASSWORD);
                            colums.Add(User.COL_DELETED);
                            colums.Add(User.COL_LOGIN);
                            conditions.Add(User.COL_EMAIL + " = " + QueryGenerator.QuoteString(email));
                            statement = QueryGenerator.GenerateSqlSelect(colums, User.TABLE, conditions);

                            cmd.CommandType = CommandType.Text;
                            cmd.CommandText = statement;

                            using (SqlDataReader dr = cmd.ExecuteReader())
                            {
                                while (dr.Read())
                                {
                                    user.Email = dr.GetString(0);
                                    user.Password = dr.GetString(1);
                                    user.Deleted = dr.GetBoolean(2);
                                    user.Login = dr.GetBoolean(3);
                                }
                                dr.Close();
                            }
                        }
                        con.Close();
                    }
                }
                catch (Exception e)
                {
                    actionContext.Response = new System.Net.Http.HttpResponseMessage(System.Net.HttpStatusCode.InternalServerError);
                }

                if (email.Equals(user.Email)
                    && password.Equals(user.Password)
                    && !user.Deleted
                    && user.Login)
                {
                    HttpContext.Current.User = new GenericPrincipal(new ApiIdentity(user), new string[] { });
                    base.OnActionExecuting(actionContext);
                }
                else
                {
                    actionContext.Response = new System.Net.Http.HttpResponseMessage(System.Net.HttpStatusCode.Unauthorized);
                }
            }
        }
    }
}