using System;
using System.Collections;
using System.Configuration;
using System.Data;
using System.Text;

namespace Utilities.QueryGenerator
{
    public static class QueryGenerator
    {
        public const string KW_SELECT = "SELECT";
        public const string KW_FROM = "FROM";
        public const string KW_INSERT = "INSERT";
        public const string KW_INTO = "INTO";
        public const string KW_VALUE = "VALUES";
        public const string KW_UPDATE = "UPDATE";
        public const string KW_SET = "SET";
        public const string KW_WHERE = "WHERE";
        public const string KW_AND = "AND";
        public const string KW_OR = "OR";
        public const string SPACE = " ";
        public const string EQUALS = " = ";
        public const string COMMA = ", ";
        public const string SQRBRC_OPEN = "[";
        public const string SQRBRC_CLOSE = "]";
        public const string PRTH_OPEN = "(";
        public const string PRTH_CLOSE = ")";
        public const string SEMI_COLON = ";";
        public const string CON_STR_NAME = "ConStarBurst_Test_GuardianNewWest";

        private static string conStr = ConfigurationManager.ConnectionStrings[CON_STR_NAME].ToString();

        public static string ConnectionString()
        {
            return conStr;
        }

        public static string LinkedUserTable()
        {
            return "LinkedUser";
        }

        public static string LocationTable()
        {
            return "Location";
        }

        public static string GenerateSqlSelect(ArrayList columns,
                                            string targetTableName,
                                            ArrayList conditions)
        {
            string statement = string.Empty;
            StringBuilder sbStatement = new StringBuilder(string.Empty);

            // SELECT
            sbStatement.Append(KW_SELECT);
            sbStatement.Append(SPACE);
            if (columns.Count > 0)
            {
                for (int i = 0; i < columns.Count; ++i)
                {
                    sbStatement.Append(columns[i]);
                    if (i != columns.Count - 1)
                        sbStatement.Append(COMMA);
                }
            }
            else
            {
                sbStatement.Append("*");
            }
            sbStatement.Append(SPACE);

            // FROM
            sbStatement.Append(KW_FROM);
            sbStatement.Append(SPACE);
            sbStatement.Append(SQRBRC_OPEN);
            sbStatement.Append(targetTableName);
            sbStatement.Append(SQRBRC_CLOSE);
            sbStatement.Append(SPACE);

            // WHERE
            if (conditions.Count > 0)
            {
                sbStatement.Append(KW_WHERE);
                sbStatement.Append(SPACE);

                foreach (string condition in conditions)
                {
                    if (condition.Equals(KW_AND) || condition.Equals(KW_OR))
                    {
                        sbStatement.Append(SPACE);
                        sbStatement.Append(condition);
                        sbStatement.Append(SPACE);
                    }
                    else
                    {
                        sbStatement.Append(PRTH_OPEN);
                        sbStatement.Append(condition);
                        sbStatement.Append(PRTH_CLOSE);
                    }
                }
            }
            sbStatement.Append(SEMI_COLON);

            return sbStatement.ToString();
        }

        public static string GenerateSqlInsert(ArrayList values,
                                            string targetTableName)
        {
            string statement = string.Empty;
            StringBuilder sbStatement = new StringBuilder(string.Empty);

            // INSERT
            sbStatement.Append(KW_INSERT);
            sbStatement.Append(SPACE);

            // INTO
            sbStatement.Append(KW_INTO);
            sbStatement.Append(SPACE);
            sbStatement.Append(SQRBRC_OPEN);
            sbStatement.Append(targetTableName);
            sbStatement.Append(SQRBRC_CLOSE);
            sbStatement.Append(SPACE);

            // VALUE
            if (values.Count > 0)
            {
                sbStatement.Append(KW_VALUE);
                sbStatement.Append(SPACE);
                sbStatement.Append(PRTH_OPEN);
                for (int i = 0; i < values.Count; ++i)
                {
                    sbStatement.Append(values[i]);
                    if (i != values.Count - 1)
                        sbStatement.Append(COMMA);
                }
                sbStatement.Append(PRTH_CLOSE);
            }

            sbStatement.Append(SEMI_COLON);

            return sbStatement.ToString();
        }

        public static string GenerateSqlUpdate(string targetTableName,
                                            ArrayList assignments,
                                            ArrayList conditions)
        {
            string statement = string.Empty;
            StringBuilder sbStatement = new StringBuilder(string.Empty);

            // UPDATE
            sbStatement.Append(KW_UPDATE);
            sbStatement.Append(SPACE);
            sbStatement.Append(SQRBRC_OPEN);
            sbStatement.Append(targetTableName);
            sbStatement.Append(SQRBRC_CLOSE);
            sbStatement.Append(SPACE);

            // SET
            if (assignments.Count > 0)
            {
                sbStatement.Append(KW_SET);
                sbStatement.Append(SPACE);
                for (int i = 0; i < assignments.Count; ++i)
                {
                    sbStatement.Append(assignments[i]);
                    if (i != assignments.Count - 1)
                        sbStatement.Append(COMMA);
                }
            }
            sbStatement.Append(SPACE);

            // WHERE
            if (conditions.Count > 0)
            {
                sbStatement.Append(KW_WHERE);
                sbStatement.Append(SPACE);

                foreach (string condition in conditions)
                {
                    if (condition.Equals(KW_AND) || condition.Equals(KW_OR))
                    {
                        sbStatement.Append(SPACE);
                        sbStatement.Append(condition);
                        sbStatement.Append(SPACE);
                    }
                    else
                    {
                        sbStatement.Append(PRTH_OPEN);
                        sbStatement.Append(condition);
                        sbStatement.Append(PRTH_CLOSE);
                    }
                }
            }
            sbStatement.Append(SEMI_COLON);

            return sbStatement.ToString();
        }

        public static string GenerateSqlDeletes(ArrayList aryColumns,
                                                DataTable dtTable,
                                                string sTargetTableName)
        {
            string sSqlDeletes = string.Empty;
            StringBuilder sbSqlStatements = new StringBuilder(string.Empty);

            // loop thru each record of the datatable
            foreach (DataRow drow in dtTable.Rows)
            {
                // loop thru each column, and include 
                // the value if the column is in the array
                string sValues = string.Empty;
                foreach (string col in aryColumns)
                {
                    string sNewValue = col + " = ";
                    if (sValues != string.Empty)
                        sValues += " AND ";

                    // need to do a case to check the column-value types
                    // (quote strings(check for dups first), convert bools)
                    string sType = string.Empty;
                    try
                    {
                        sType = drow[col].GetType().ToString();
                        switch (sType.Trim().ToLower())
                        {
                            case "system.boolean":
                                sNewValue += (Convert.ToBoolean(drow[col]) ==
                                              true ? "1" : "0");
                                break;

                            case "system.string":
                                sNewValue += string.Format("'{0}'",
                                             ConvertQuote(drow[col]));
                                break;

                            case "system.datetime":
                                sNewValue += string.Format("'{0}'",
                                             ConvertQuote(drow[col]));
                                break;

                            default:
                                if (drow[col] == System.DBNull.Value)
                                    sNewValue += "NULL";
                                else
                                    sNewValue += Convert.ToString(drow[col]);
                                break;
                        }
                    }
                    catch
                    {
                        sNewValue += string.Format("'{0}'",
                                     ConvertQuote(drow[col]));
                    }

                    sValues += sNewValue;
                }

                // DELETE FROM table WHERE col1 = 3 AND col2 = '4'
                // write the line out to the stringbuilder
                string snewsql = string.Format("DELETE FROM {0} WHERE {1};",
                                                sTargetTableName, sValues);
                sbSqlStatements.Append(snewsql);
                sbSqlStatements.AppendLine();
                sbSqlStatements.AppendLine();
            }

            sSqlDeletes = sbSqlStatements.ToString();
            return sSqlDeletes;
        }

        public static string ConvertQuote(object ostr)
        {
            return ostr.ToString().Replace("'", "''");
        }

        public static string ConvertQuote(string str)
        {
            return str.Replace("'", "''");
        }

        public static string QuoteString(string str)
        {
            return ("'" + str + "'");
        }
    }
}