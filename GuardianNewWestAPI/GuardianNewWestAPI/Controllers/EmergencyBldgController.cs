using System;
using System.Web.Http;
using System.Data.SqlClient;
using System.Data;
using System.Collections;
using GuardianNewWestAPI.Models;
using GuardianNewWestAPI.Utilities;

namespace GuardianNewWestAPI.Controllers
{
    public class EmergencyBldgController : ApiController
    {
        [HttpGet]
        //[BasicAuthentication]
        [AcceptVerbs("GET")]
        [Route("~/emergencybldg/get/all")]
        public IHttpActionResult GetEmergencyBldgAll()
        {
            ArrayList buildings = new ArrayList();
            EmergencyBldg bldg;

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

                        colums.Add(EmergencyBldg.COL_ID);
                        colums.Add(EmergencyBldg.COL_CATEGORY);
                        colums.Add(EmergencyBldg.COL_BLDGID);
                        colums.Add(EmergencyBldg.COL_BLDGNAME);
                        colums.Add(EmergencyBldg.COL_STRNUM);
                        colums.Add(EmergencyBldg.COL_STRNAME);
                        colums.Add(EmergencyBldg.COL_MAPREF);
                        colums.Add(EmergencyBldg.COL_LAT);
                        colums.Add(EmergencyBldg.COL_LNG);
                        colums.Add(EmergencyBldg.COL_PHONE);
                        colums.Add(EmergencyBldg.COL_LOCNAME);
                        statement = QueryGenerator.GenerateSqlSelect(colums,
                            EmergencyBldg.TABLE, conditions);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement;

                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                bldg = new EmergencyBldg();
                                int i = 0;
                                bldg.ID = dr.GetInt32(i);
                                i++;
                                bldg.Category = dr.GetInt32(i);
                                i++;
                                bldg.BldgID = dr.GetInt32(i);
                                i++;
                                bldg.BldgName = dr.GetString(i);
                                i++;
                                bldg.StrNum = dr.GetInt32(i);
                                i++;
                                bldg.StrName = dr.GetString(i);
                                i++;
                                bldg.MapRef = dr.GetInt32(i);
                                i++;
                                bldg.Lat = dr.GetDouble(i);
                                i++;
                                bldg.Lng = dr.GetDouble(i);
                                i++;
                                if (dr.GetValue(i) != DBNull.Value)
                                    bldg.Phone = dr.GetInt64(i);
                                i++;
                                if (dr.GetValue(i) != DBNull.Value)
                                    bldg.LocName = dr.GetString(i);
                                buildings.Add(bldg);
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

            if (buildings.Count == 0)
                return ResponseMessage(JsonContent.ReturnMessage("No building is found.", ""));
            return Ok(new { buildings });
        }
    }
}
