using System;
using System.Collections;
using System.Data;
using System.Data.SqlClient;
using System.Security.Principal;
using System.Text;
using System.Web;
using UserList.Models;
using Utilities.QueryGenerator;
using Filters.ApiIdentityModel;

namespace Filters.BasicAuthenticationAttribute
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
                string username = decodedToken.Substring(0, decodedToken.IndexOf(":"));
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

                            colums.Add("UserName");
                            colums.Add("Password");
                            colums.Add("Del");
                            conditions.Add("UserName = " + QueryGenerator.QuoteString(username));
                            statement = QueryGenerator.GenerateSqlSelect(colums, QueryGenerator.UserTable(), conditions);

                            cmd.CommandType = CommandType.Text;
                            cmd.CommandText = statement;

                            using (SqlDataReader dr = cmd.ExecuteReader())
                            {
                                while (dr.Read())
                                {
                                    user.UserName = dr.GetString(0);
                                    user.Password = dr.GetString(1);
                                    user.Deleted = dr.GetBoolean(2);
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

                if (username.Equals(user.UserName)
                    && password.Equals(user.Password)
                    && !user.Deleted)
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