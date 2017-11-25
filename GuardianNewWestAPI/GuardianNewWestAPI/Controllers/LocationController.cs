using System;
using System.Web.Http;
using System.Data.SqlClient;
using System.Data;
using System.Collections;
using System.Linq;
using GuardianNewWestAPI.Models;
using GuardianNewWestAPI.Utilities;
using GuardianNewWestAPI.Filters;

namespace GuardianNewWestAPI.Controllers
{
    public class LocationController : ApiController
    {
        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/location/get")]
        public IHttpActionResult GetLocationById([FromBody] object data)
        {
            Location location = new Location();

            try
            {
                var headers = Request.Headers;
                string id = headers.GetValues(Models.User.COL_ID).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList colums = new ArrayList();
                        ArrayList conditions = new ArrayList();
                        ArrayList orders = new ArrayList();
                        string statement = string.Empty;

                        colums.Add(Location.COL_ID);
                        colums.Add(Location.COL_USERID);
                        colums.Add(Location.COL_LAT);
                        colums.Add(Location.COL_LNG);
                        colums.Add(Location.COL_ALERTTIME);
                        conditions.Add(Location.COL_USERID + "=" + id);
                        orders.Add(Location.COL_ALERTTIME);
                        statement = QueryGenerator.GenerateSqlSelect(colums,
                                                                     Location.TABLE,
                                                                     conditions,
                                                                     orders,
                                                                     QueryGenerator.KW_DSC,
                                                                     1);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement;

                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                int i = 0;
                                location.ID = dr.GetInt32(i);
                                i++;
                                location.UserID = dr.GetInt32(i);
                                i++;
                                location.Lat = dr.GetDouble(i);
                                i++;
                                location.Lng = dr.GetDouble(i);
                                i++;
                                location.AlertTime = dr.GetDateTime(i);
                            }
                            dr.Close();
                        }
                    }
                    con.Close();
                }
            }
            catch (Exception e)
            {
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            if (location.ID == 0)
                return ResponseMessage(JsonContent.ReturnMessage("No location is found.", ""));
            return Ok(new { location });
        }

        [HttpPost]
        [AcceptVerbs("GET", "POST")]
        [Route("~/location/create")]
        public IHttpActionResult CreateLocation([FromBody] object data)
        {
            try
            {
                var headers = Request.Headers;
                string id = headers.GetValues(Models.User.COL_ID).First();
                string lat = headers.GetValues(Location.COL_LAT).First();
                string lng = headers.GetValues(Location.COL_LNG).First();
                string time = headers.GetValues(Location.COL_ALERTTIME).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList values = new ArrayList();
                        ArrayList assignments = new ArrayList();
                        string statement = string.Empty;

                        values.Add(id);
                        values.Add(lat);
                        values.Add(lng);
                        values.Add(QueryGenerator.QuoteString(time));
                        statement = QueryGenerator.GenerateSqlInsert(values, Location.TABLE);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement;
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