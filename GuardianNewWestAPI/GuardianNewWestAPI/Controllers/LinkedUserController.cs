using System;
using System.Web.Http;
using System.Data.SqlClient;
using System.Data;
using System.Collections;
using Utilities.QueryGenerator;
using Utilities.JsonContent;
using Filters.BasicAuthenticationAttribute;
using System.Linq;

namespace Models
{
    public class LinkedUserController : ApiController
    {
        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/linkeduser/get/all")]
        public IHttpActionResult GetLinkedUsersById([FromBody] object data)
        {
            ArrayList linkedUsers = new ArrayList();
            LinkedUser.LinkedUser lu;

            try
            {
                var headers = Request.Headers;
                string id = headers.GetValues(Models.User.User.COL_ID).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        // query linked user list
                        ArrayList columsS1 = new ArrayList();
                        ArrayList conditionsS1 = new ArrayList();
                        string statement1 = string.Empty;

                        columsS1.Add(LinkedUser.LinkedUser.COL_ID);
                        columsS1.Add(LinkedUser.LinkedUser.COL_USERID1);
                        columsS1.Add(LinkedUser.LinkedUser.COL_USERID2);
                        columsS1.Add(LinkedUser.LinkedUser.COL_ALERT1);
                        columsS1.Add(LinkedUser.LinkedUser.COL_ALERT2);
                        columsS1.Add(LinkedUser.LinkedUser.COL_MUTE1);
                        columsS1.Add(LinkedUser.LinkedUser.COL_MUTE2);
                        columsS1.Add(LinkedUser.LinkedUser.COL_DELETED);
                        columsS1.Add(LinkedUser.LinkedUser.COL_ADDED1);
                        columsS1.Add(LinkedUser.LinkedUser.COL_ADDED2);
                        conditionsS1.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + id);
                        conditionsS1.Add(QueryGenerator.KW_OR);
                        conditionsS1.Add(LinkedUser.LinkedUser.COL_USERID2 + "=" + id);
                        statement1 = QueryGenerator.GenerateSqlSelect(columsS1, LinkedUser.LinkedUser.TABLE, conditionsS1);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement1;

                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                lu = new LinkedUser.LinkedUser();
                                int i = 0;
                                lu.ID = dr.GetInt32(i);
                                i++;
                                lu.UserIDMe = dr.GetInt32(i);
                                if (lu.UserIDMe == int.Parse(id))
                                {
                                    lu.UserIDMe = dr.GetInt32(i);
                                    i++;
                                    lu.UserIDTarget = dr.GetInt32(i);
                                    i++;
                                    lu.AlertMe = dr.GetBoolean(i);
                                    i++;
                                    lu.AlertTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.MuteMe = dr.GetBoolean(i);
                                    i++;
                                    lu.MuteTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.Deleted = dr.GetBoolean(i);
                                    i++;
                                    lu.AddedMe = dr.GetBoolean(i);
                                    i++;
                                    lu.AddedTarget = dr.GetBoolean(i);
                                }
                                else
                                {
                                    lu.UserIDTarget = dr.GetInt32(i);
                                    i++;
                                    lu.UserIDMe = dr.GetInt32(i);
                                    i++;
                                    lu.AlertTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.AlertMe = dr.GetBoolean(i);
                                    i++;
                                    lu.MuteTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.MuteMe = dr.GetBoolean(i);
                                    i++;
                                    lu.Deleted = dr.GetBoolean(i);
                                    i++;
                                    lu.AddedTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.AddedMe = dr.GetBoolean(i);
                                }

                                linkedUsers.Add(lu);
                            }
                            dr.Close();
                        }
                        linkedUsers.Sort();

                        // query linked user name
                        ArrayList columsS2 = new ArrayList();
                        ArrayList conditionsS2 = new ArrayList();
                        ArrayList ordersS2 = new ArrayList();
                        string statement2 = string.Empty;

                        columsS2.Add(Models.User.User.COL_ID);
                        columsS2.Add(Models.User.User.COL_DELETED);
                        columsS2.Add(Models.User.User.COL_USERNAME);
                        for (int i = 0; i < linkedUsers.Count; ++i)
                        {
                            if (i > 0)
                            {
                                conditionsS2.Add(QueryGenerator.KW_OR);
                            }
                            conditionsS2.Add(Models.User.User.COL_ID + "=" + ((LinkedUser.LinkedUser)linkedUsers[i]).UserIDTarget);
                        }
                        ordersS2.Add(Models.User.User.COL_ID);
                        statement2 = QueryGenerator.GenerateSqlSelect(columsS2, Models.User.User.TABLE, conditionsS2, ordersS2, QueryGenerator.KW_ASC);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement2;

                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            int i = 0;

                            while (dr.Read())
                            {
                                if (((LinkedUser.LinkedUser)linkedUsers[i]).UserIDTarget == dr.GetInt32(0)
                                    && !dr.GetBoolean(1))
                                {
                                    ((LinkedUser.LinkedUser)linkedUsers[i]).NameTarget = dr.GetString(2);
                                }
                                else
                                {
                                    ((LinkedUser.LinkedUser)linkedUsers[i]).NameTarget = string.Empty;
                                }
                                i++;
                            }
                            dr.Close();
                        }
                        for (int i = linkedUsers.Count - 1; i >= 0; --i)
                        {
                            if (((LinkedUser.LinkedUser)linkedUsers[i]).NameTarget.Length == 0)
                            {
                                linkedUsers.RemoveAt(i);
                            }
                        }
                    }
                    con.Close();
                }
            }
            catch (Exception e)
            {
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            if (linkedUsers.Count == 0)
                return ResponseMessage(JsonContent.ReturnMessage("No linked-user is found.", ""));
            return Ok(new { linkedUsers });
        }

        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/linkeduser/alert")]
        public IHttpActionResult SetLinkedUserAlert([FromBody] object data)
        {
            try
            {
                var headers = Request.Headers;
                string idMe = headers.GetValues(Models.User.User.COL_ID).First();
                string idTarget = headers.GetValues(LinkedUser.LinkedUser.PARAM_TARGET).First();
                string alert = headers.GetValues(LinkedUser.LinkedUser.PARAM_ALERT).First();

                if (alert.ToLower() == "true")
                {
                    alert = "1";
                }
                else if (alert.ToLower() == "false")
                {
                    alert = "0";
                }

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        // condition 1
                        string c1;
                        ArrayList columnsC1 = new ArrayList();
                        ArrayList conditionsC1 = new ArrayList();
                        columnsC1.Add(LinkedUser.LinkedUser.COL_USERID1);
                        conditionsC1.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idMe
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idTarget);
                        conditionsC1.Add(QueryGenerator.KW_OR);
                        conditionsC1.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idTarget
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idMe);
                        c1 = QueryGenerator.GenerateSqlSelect(columnsC1, LinkedUser.LinkedUser.TABLE, conditionsC1
                            , null, QueryGenerator.KW_ASC, 1);
                        c1 = QueryGenerator.ParenthesisString(c1) + "=" + idMe;

                        // conditions
                        ArrayList conditions = new ArrayList();
                        conditions.Add(c1);

                        // statement 1
                        ArrayList assignS1 = new ArrayList();
                        ArrayList conditionS1 = new ArrayList();
                        string s1;
                        assignS1.Add(LinkedUser.LinkedUser.COL_ALERT1 + "=" + alert);
                        conditionS1.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idMe
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idTarget);
                        s1 = QueryGenerator.GenerateSqlUpdate(LinkedUser.LinkedUser.TABLE, assignS1, conditionS1);

                        // statement 2
                        ArrayList assignS2 = new ArrayList();
                        ArrayList conditionS2 = new ArrayList();
                        string s2;
                        assignS2.Add(LinkedUser.LinkedUser.COL_ALERT2 + "=" + alert);
                        conditionS2.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idTarget
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idMe);
                        s2 = QueryGenerator.GenerateSqlUpdate(LinkedUser.LinkedUser.TABLE, assignS2, conditionS2);

                        string statement;
                        statement = QueryGenerator.GenerateSqlIfElse(conditions, s1, s2);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement.ToString();
                        cmd.ExecuteNonQuery();
                    }
                    con.Close();
                }
            }
            catch
            {
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            return ResponseMessage(JsonContent.ReturnMessage("The request is processed.", ""));
        }

        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/linkeduser/mute")]
        public IHttpActionResult SetLinkedUserMute([FromBody] object data)
        {
            try
            {
                var headers = Request.Headers;
                string idMe = headers.GetValues(Models.User.User.COL_ID).First();
                string idTarget = headers.GetValues(LinkedUser.LinkedUser.PARAM_TARGET).First();
                string mute = headers.GetValues(LinkedUser.LinkedUser.PARAM_MUTE).First();

                if (mute.ToLower() == "true")
                {
                    mute = "1";
                }
                else if (mute.ToLower() == "false")
                {
                    mute = "0";
                }

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        // condition 1
                        string c1;
                        ArrayList columnsC1 = new ArrayList();
                        ArrayList conditionsC1 = new ArrayList();
                        columnsC1.Add(LinkedUser.LinkedUser.COL_USERID1);
                        conditionsC1.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idMe
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idTarget);
                        conditionsC1.Add(QueryGenerator.KW_OR);
                        conditionsC1.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idTarget
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idMe);
                        c1 = QueryGenerator.GenerateSqlSelect(columnsC1, LinkedUser.LinkedUser.TABLE, conditionsC1
                            , null, QueryGenerator.KW_ASC, 1);
                        c1 = QueryGenerator.ParenthesisString(c1) + "=" + idMe;

                        // conditions
                        ArrayList conditions = new ArrayList();
                        conditions.Add(c1);

                        // statement 1
                        ArrayList assignS1 = new ArrayList();
                        ArrayList conditionS1 = new ArrayList();
                        string s1;
                        assignS1.Add(LinkedUser.LinkedUser.COL_MUTE1 + "=" + mute);
                        conditionS1.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idMe
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idTarget);
                        s1 = QueryGenerator.GenerateSqlUpdate(LinkedUser.LinkedUser.TABLE, assignS1, conditionS1);

                        // statement 2
                        ArrayList assignS2 = new ArrayList();
                        ArrayList conditionS2 = new ArrayList();
                        string s2;
                        assignS2.Add(LinkedUser.LinkedUser.COL_MUTE2 + "=" + mute);
                        conditionS2.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idTarget
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idMe);
                        s2 = QueryGenerator.GenerateSqlUpdate(LinkedUser.LinkedUser.TABLE, assignS2, conditionS2);

                        string statement;
                        statement = QueryGenerator.GenerateSqlIfElse(conditions, s1, s2);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement.ToString();
                        cmd.ExecuteNonQuery();
                    }
                    con.Close();
                }
            }
            catch
            {
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            return ResponseMessage(JsonContent.ReturnMessage("The request is processed.", ""));
        }

        [HttpPost]
        [AcceptVerbs("GET", "POST")]
        [Route("~/linkeduser/add")]
        public IHttpActionResult AddLinkedUser([FromBody] object data)
        {
            try
            {
                var headers = Request.Headers;
                string idMe = headers.GetValues(Models.User.User.COL_ID).First();
                string idTarget = headers.GetValues(LinkedUser.LinkedUser.PARAM_TARGET).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        // conditions 1
                        ArrayList conditions1 = new ArrayList();
                        string c1;
                        ArrayList columnsC1 = new ArrayList();
                        ArrayList conditionsC1 = new ArrayList();
                        columnsC1.Add(LinkedUser.LinkedUser.COL_ID);
                        conditionsC1.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idMe
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idTarget);
                        c1 = QueryGenerator.GenerateSqlSelect(columnsC1, LinkedUser.LinkedUser.TABLE, conditionsC1
                            , null, QueryGenerator.KW_ASC, 1);
                        c1 = QueryGenerator.KW_EXISTS + QueryGenerator.SPACE + QueryGenerator.ParenthesisString(c1);
                        conditions1.Add(c1);

                        // conditions 2
                        ArrayList conditions2 = new ArrayList();
                        string c2;
                        ArrayList columnsC2 = new ArrayList();
                        ArrayList conditionsC2 = new ArrayList();
                        columnsC2.Add(LinkedUser.LinkedUser.COL_ID);
                        conditionsC2.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idTarget
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idMe);
                        c2 = QueryGenerator.GenerateSqlSelect(columnsC2, LinkedUser.LinkedUser.TABLE, conditionsC2
                            , null, QueryGenerator.KW_ASC, 1);
                        c2 = QueryGenerator.KW_EXISTS + QueryGenerator.SPACE + QueryGenerator.ParenthesisString(c2);
                        conditions2.Add(c2);

                        // statement 1
                        ArrayList assignS1 = new ArrayList();
                        ArrayList conditionS1 = new ArrayList();
                        string s1;
                        assignS1.Add(LinkedUser.LinkedUser.COL_ADDED1 + "=1");
                        conditionS1.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idMe
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idTarget);
                        s1 = QueryGenerator.GenerateSqlUpdate(LinkedUser.LinkedUser.TABLE, assignS1, conditionS1);

                        // statement 2
                        ArrayList assignS2 = new ArrayList();
                        ArrayList conditionS2 = new ArrayList();
                        string s2;
                        assignS2.Add(LinkedUser.LinkedUser.COL_ADDED2 + "=1");
                        conditionS2.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idTarget
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idMe);
                        s2 = QueryGenerator.GenerateSqlUpdate(LinkedUser.LinkedUser.TABLE, assignS2, conditionS2);

                        // statement 3
                        ArrayList valuesS3 = new ArrayList();
                        string s3 = string.Empty;

                        valuesS3.Add(idMe);
                        valuesS3.Add(idTarget);
                        valuesS3.Add("1"); // Alert1
                        valuesS3.Add("0"); // Alert2
                        valuesS3.Add("0"); // Mute1
                        valuesS3.Add("0"); // Mute2
                        valuesS3.Add("0"); // Del
                        valuesS3.Add("1"); // Added1
                        valuesS3.Add("0"); // Added2
                        s3 = QueryGenerator.GenerateSqlInsert(valuesS3, LinkedUser.LinkedUser.TABLE);

                        string statement;
                        statement = QueryGenerator.GenerateSqlIfElseIfElse(conditions1, conditions2, s1, s2, s3);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement.ToString();
                        cmd.ExecuteNonQuery();
                    }
                    con.Close();
                }
            }
            catch
            {
                //return ResponseMessage(Request.CreateResponse(HttpStatusCode.BadRequest, false));
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            //return ResponseMessage(Request.CreateResponse(HttpStatusCode.OK, true));
            return ResponseMessage(JsonContent.ReturnMessage("The request is processed.", ""));
        }

        [HttpPost]
        [AcceptVerbs("GET", "POST")]
        [Route("~/linkeduser/remove")]
        public IHttpActionResult RemoveLinkedUser([FromBody] object data)
        {
            try
            {
                var headers = Request.Headers;
                string idMe = headers.GetValues(Models.User.User.COL_ID).First();
                string idTarget = headers.GetValues(LinkedUser.LinkedUser.PARAM_TARGET).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        // conditions 1
                        ArrayList conditions1 = new ArrayList();
                        string c1;
                        ArrayList columnsC1 = new ArrayList();
                        ArrayList conditionsC1 = new ArrayList();
                        columnsC1.Add(LinkedUser.LinkedUser.COL_ID);
                        conditionsC1.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idMe
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idTarget);
                        c1 = QueryGenerator.GenerateSqlSelect(columnsC1, LinkedUser.LinkedUser.TABLE, conditionsC1
                            , null, QueryGenerator.KW_ASC, 1);
                        c1 = QueryGenerator.KW_EXISTS + QueryGenerator.SPACE + QueryGenerator.ParenthesisString(c1);
                        conditions1.Add(c1);

                        // conditions 2
                        ArrayList conditions2 = new ArrayList();
                        string c2;
                        ArrayList columnsC2 = new ArrayList();
                        ArrayList conditionsC2 = new ArrayList();
                        columnsC2.Add(LinkedUser.LinkedUser.COL_ID);
                        conditionsC2.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idTarget
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idMe);
                        c2 = QueryGenerator.GenerateSqlSelect(columnsC2, LinkedUser.LinkedUser.TABLE, conditionsC2
                            , null, QueryGenerator.KW_ASC, 1);
                        c2 = QueryGenerator.KW_EXISTS + QueryGenerator.SPACE + QueryGenerator.ParenthesisString(c2);
                        conditions2.Add(c2);

                        // statement 1
                        ArrayList assignS1 = new ArrayList();
                        ArrayList conditionS1 = new ArrayList();
                        string s1;
                        assignS1.Add(LinkedUser.LinkedUser.COL_ADDED1 + "=0");
                        assignS1.Add(LinkedUser.LinkedUser.COL_DELETED + "=1");
                        conditionS1.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idMe
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idTarget);
                        s1 = QueryGenerator.GenerateSqlUpdate(LinkedUser.LinkedUser.TABLE, assignS1, conditionS1);

                        // statement 2
                        ArrayList assignS2 = new ArrayList();
                        ArrayList conditionS2 = new ArrayList();
                        string s2;
                        assignS2.Add(LinkedUser.LinkedUser.COL_ADDED2 + "=0");
                        assignS2.Add(LinkedUser.LinkedUser.COL_DELETED + "=1");
                        conditionS2.Add(LinkedUser.LinkedUser.COL_USERID1 + "=" + idTarget
                            + QueryGenerator.SPACE + QueryGenerator.KW_AND + QueryGenerator.SPACE
                            + LinkedUser.LinkedUser.COL_USERID2 + "=" + idMe);
                        s2 = QueryGenerator.GenerateSqlUpdate(LinkedUser.LinkedUser.TABLE, assignS2, conditionS2);

                        // statement 3
                        string s3 = QueryGenerator.KW_DONOTHING;

                        string statement;
                        statement = QueryGenerator.GenerateSqlIfElseIfElse(conditions1, conditions2, s1, s2, s3);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement.ToString();
                        cmd.ExecuteNonQuery();
                    }
                    con.Close();
                }
            }
            catch
            {
                //return ResponseMessage(Request.CreateResponse(HttpStatusCode.BadRequest, false));
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            //return ResponseMessage(Request.CreateResponse(HttpStatusCode.OK, true));
            return ResponseMessage(JsonContent.ReturnMessage("The request is processed.", ""));
        }
    }
}