package databaseInterface;

class Queries2 {
	public static final String qa = "-- Query a\r\n" + 
			"WITH TEMP1 AS (SELECT ac2.PERSONID, ac2.CLIPID\r\n" + 
			"              FROM acted ac2\r\n" + 
			"              WHERE ac2.PERSONID IN (SELECT ac1.PERSONID\r\n" + 
			"                    FROM ACTED ac1\r\n" + 
			"                    GROUP BY ac1.PERSONID\r\n" + 
			"                    HAVING COUNT(ac1.PERSONID)>=5)),\r\n" + 
			"     TEMP2 AS (SELECT rt.CLIPID, rt.RANK, rt.VOTES\r\n" + 
			"               FROM RATINGS rt\r\n" + 
			"               WHERE rt.VOTES >= 100),\r\n" + 
			"     TEMP3 AS (SELECT TEMP1.PERSONID, TEMP1.CLIPID, TEMP2.RANK, TEMP2.VOTES\r\n" + 
			"              FROM TEMP1\r\n" + 
			"              INNER JOIN TEMP2 ON TEMP1.CLIPID = TEMP2.CLIPID),\r\n" + 
			"     TEMP4 AS (SELECT f.PERSONID, f.CLIPID, f.RANK, f.VOTES\r\n" + 
			"               FROM  (SELECT PERSONID, CLIPID, RANK, VOTES, ROW_NUMBER() OVER(PARTITION BY PERSONID ORDER BY RANK desc) as rn\r\n" + 
			"                      FROM TEMP3) f\r\n" + 
			"               WHERE rn <= 3),\r\n" + 
			"     TEMP5 AS (SELECT tmp4.PERSONID, tmp4.CLIPID, tmp4.RANK\r\n" + 
			"              FROM (SELECT tmp4_bis.PERSONID\r\n" + 
			"                    FROM TEMP4 tmp4_bis\r\n" + 
			"                    GROUP BY tmp4_bis.PERSONID\r\n" + 
			"                    HAVING COUNT(tmp4_bis.PERSONID)=3) tmp_count, \r\n" + 
			"                    TEMP4 tmp4\r\n" + 
			"              WHERE tmp_count.PERSONID=tmp4.PERSONID) \r\n" + 
			"SELECT pp.FULLNAME\r\n" + 
			"FROM (SELECT DISTINCT(PERSONID), (AVG(RANK) OVER(PARTITION BY PERSONID)) as average \r\n" + 
			"      FROM TEMP5 \r\n" + 
			"      ORDER BY average DESC) avg_table, PEOPLE pp\r\n" + 
			"WHERE pp.PERSONID = avg_table.PERSONID\r\n" + 
			"FETCH FIRST 10 ROWS ONLY";
	
	public static final String qb = "WITH TEMP1 AS (SELECT CLIPS.CLIPID, FLOOR(EXTRACT(YEAR FROM CLIPS.CLIPYEAR)/10)*10 as YEAR, RATINGS.RANK\r\n" + 
			"               FROM RATINGS\r\n" + 
			"               INNER JOIN CLIPS ON RATINGS.CLIPID = CLIPS.CLIPID),\r\n" + 
			"    TEMP2 AS (SELECT CLIPID, YEAR, AVG(RANK) as RANK \r\n" + 
			"              FROM TEMP1 \r\n" + 
			"              GROUP BY CLIPID, YEAR),\r\n" + 
			"    TEMP3 AS (SELECT CLIPID, YEAR, RANK\r\n" + 
			"              FROM (SELECT CLIPID, YEAR, RANK, ROW_NUMBER() OVER(PARTITION BY YEAR ORDER BY RANK desc) as rn \r\n" + 
			"                    FROM TEMP1)\r\n" + 
			"              WHERE rn <= 100)\r\n" + 
			"SELECT YEAR, AVG(RANK)\r\n" + 
			"FROM TEMP3\r\n" + 
			"WHERE YEAR IS NOT NULL\r\n" + 
			"GROUP BY YEAR\r\n" + 
			"ORDER BY AVG(RANK) DESC";
	
	public static final String qc = "WITH TEMP1 AS (SELECT DIRECTED.PERSONID, DIRECTED.CLIPID, vidg.CLIPYEAR\r\n" + 
			"               FROM(SELECT CLIPID, CLIPYEAR\r\n" + 
			"                    FROM CLIPS\r\n" + 
			"                    WHERE CLIPTYPE = 'VG') vidg\r\n" + 
			"               INNER JOIN DIRECTED ON vidg.CLIPID=DIRECTED.CLIPID),\r\n" + 
			"     TEMP2 AS (SELECT PERSONID, CLIPYEAR\r\n" + 
			"               FROM (SELECT PERSONID, CLIPYEAR, ROW_NUMBER() OVER(PARTITION BY PERSONID ORDER BY CLIPYEAR asc) as rn\r\n" + 
			"                     FROM TEMP1)\r\n" + 
			"               WHERE rn = 1)\r\n" + 
			"SELECT pp.FULLNAME, cl.CLIPTITLE, EXTRACT(YEAR FROM tmp.CLIPYEAR) AS YEAR\r\n" + 
			"FROM (SELECT TEMP1.PERSONID, TEMP1.CLIPID, TEMP1.CLIPYEAR\r\n" + 
			"      FROM TEMP2\r\n" + 
			"      INNER JOIN TEMP1 ON TEMP1.PERSONID = TEMP2.PERSONID AND TEMP1.CLIPYEAR = TEMP2.CLIPYEAR) tmp, PEOPLE pp, CLIPS cl\r\n" + 
			"WHERE pp.PERSONID = tmp.PERSONID AND cl.CLIPID = tmp.CLIPID";
	public static final String qd = "WITH TEMP1 AS (SELECT CLIPS.CLIPID, EXTRACT(YEAR FROM CLIPS.CLIPYEAR) as YEAR, CLIPS.CLIPTITLE , RATINGS.RANK\r\n" + 
			"              FROM CLIPS\r\n" + 
			"              INNER JOIN RATINGS ON RATINGS.CLIPID=CLIPS.CLIPID),\r\n" + 
			"     TEMP2 AS (SELECT CLIPID, YEAR, CLIPTITLE, RANK\r\n" + 
			"              FROM (SELECT CLIPID, YEAR, CLIPTITLE, RANK, ROW_NUMBER() OVER(PARTITION BY YEAR ORDER BY RANK DESC) as rn \r\n" + 
			"              FROM TEMP1)\r\n" + 
			"              WHERE rn <= 3 and YEAR IS NOT NULL)\r\n" + 
			"SELECT *\r\n" + 
			"FROM TEMP2";
	public static final String qe =	"WITH TEMP1 AS (SELECT DIRECTED.PERSONID, DIRECTED.CLIPID\r\n" + 
			"               FROM WROTE\r\n" + 
			"               INNER JOIN DIRECTED ON DIRECTED.PERSONID = WROTE.PERSONID),\r\n" + 
			"     TEMP2 AS (SELECT DISTINCT(ACTED.PERSONID)\r\n" + 
			"               FROM TEMP1\r\n" + 
			"               INNER JOIN ACTED ON (ACTED.PERSONID = TEMP1.PERSONID) AND (ACTED.CLIPID = TEMP1.CLIPID)),\r\n" + 
			"     TEMP3 AS (SELECT DIRECTED.PERSONID, DIRECTED.CLIPID\r\n" + 
			"               FROM TEMP2\r\n" + 
			"               INNER JOIN DIRECTED ON DIRECTED.PERSONID = TEMP2.PERSONID),\r\n" + 
			"     TEMP4 AS (SELECT WROTE.PERSONID, WROTE.CLIPID\r\n" + 
			"               FROM TEMP2\r\n" + 
			"               INNER JOIN WROTE ON WROTE.PERSONID = TEMP2.PERSONID),\r\n" + 
			"     TEMP5 AS (SELECT TEMP3.PERSONID, RATINGS.CLIPID, RATINGS.RANK\r\n" + 
			"               FROM TEMP3\r\n" + 
			"               INNER JOIN RATINGS ON RATINGS.CLIPID=TEMP3.CLIPID),\r\n" + 
			"     TEMP6 AS (SELECT TEMP4.PERSONID, RATINGS.CLIPID, RATINGS.RANK\r\n" + 
			"               FROM TEMP4\r\n" + 
			"               INNER JOIN RATINGS ON RATINGS.CLIPID=TEMP4.CLIPID),\r\n" + 
			"     TEMP7 AS (SELECT PERSONID, CLIPID, RANK\r\n" + 
			"               FROM (SELECT PERSONID, CLIPID, RANK, ROW_NUMBER() OVER(PARTITION BY PERSONID ORDER BY RANK ASC) as rn\r\n" + 
			"               FROM TEMP5)\r\n" + 
			"               WHERE rn = 1),\r\n" + 
			"     TEMP8 AS (SELECT PERSONID, CLIPID, RANK\r\n" + 
			"               FROM (SELECT PERSONID, CLIPID, RANK, ROW_NUMBER() OVER(PARTITION BY PERSONID ORDER BY RANK DESC) as rn\r\n" + 
			"               FROM TEMP6)\r\n" + 
			"               WHERE rn = 1)\r\n" + 
			"SELECT pp.FULLNAME\r\n" + 
			"FROM TEMP7 tmp7, TEMP8 tmp8, PEOPLE pp\r\n" + 
			"WHERE tmp7.PERSONID = tmp8.PERSONID AND tmp7.PERSONID = pp.PERSONID AND tmp7.RANK - tmp8.RANK >= 2";
	public static final String qf =	"WITH TEMP1 AS (SELECT BIOGRAPHY.PERSONID\r\n" + 
			"               FROM BIOGRAPHY\r\n" + 
			"               LEFT JOIN SPOUSE ON BIOGRAPHY.PERSONID = SPOUSE.PERSONID\r\n" + 
			"               WHERE SPOUSE.PERSONID IS NULL),\r\n" + 
			"     TEMP2 AS (SELECT TEMP1.PERSONID, ACTED.CLIPID\r\n" + 
			"               FROM TEMP1\r\n" + 
			"               INNER JOIN ACTED ON ACTED.PERSONID = TEMP1.PERSONID),\r\n" + 
			"     TEMP3 AS (SELECT DIRECTED.PERSONID, DIRECTED.CLIPID\r\n" + 
			"               FROM(SELECT dirole.DIRECTEDID\r\n" + 
			"                    FROM DIRECTEDROLE dirole\r\n" + 
			"                    WHERE dirole.ROLES LIKE '%co-director%') codir\r\n" + 
			"               INNER JOIN DIRECTED ON DIRECTED.DIRECTEDID = codir.DIRECTEDID),           \r\n" + 
			"     TEMP4 AS (SELECT TEMP3.PERSONID, TEMP3.CLIPID\r\n" + 
			"               FROM TEMP2\r\n" + 
			"               INNER JOIN TEMP3 ON TEMP3.PERSONID = TEMP2.PERSONID AND TEMP3.CLIPID = TEMP2.CLIPID)\r\n" + 
			"SELECT pp.FULLNAME\r\n" + 
			"FROM (SELECT PERSONID\r\n" + 
			"      FROM TEMP3 tmp3\r\n" + 
			"      GROUP BY tmp3.PERSONID\r\n" + 
			"      HAVING COUNT(tmp3.PERSONID) > 2) tmp4, PEOPLE pp\r\n" + 
			"WHERE pp.PERSONID = tmp4.PERSONID";
	public static final String qg =	"WITH TEMP1 AS (SELECT wr.PERSONID, wrole.WORKTYPES, wr.CLIPID\r\n" + 
			"               FROM WROTEROLE wrole, WROTE wr\r\n" + 
			"               WHERE wrole.WORKTYPES LIKE '%screenplay%' AND wrole.WROTEID = wr.WROTEID),\r\n" + 
			"     TEMP2 AS (SELECT tmp2.CLIPID\r\n" + 
			"               FROM(SELECT pr.PERSONID, pr.CLIPID\r\n" + 
			"                    FROM TEMP1 tmp1, PRODUCED pr\r\n" + 
			"                    WHERE tmp1.CLIPID = pr.CLIPID) tmp2\r\n" + 
			"               GROUP BY tmp2.CLIPID\r\n" + 
			"               HAVING COUNT(tmp2.CLIPID) > 2)\r\n" + 
			"SELECT DISTINCT(PEOPLE.FULLNAME)\r\n" + 
			"FROM TEMP1, TEMP2, PEOPLE\r\n" + 
			"WHERE TEMP1.CLIPID = TEMP2.CLIPID AND PEOPLE.PERSONID = TEMP1.PERSONID";
	public static final String qh =	"WITH TEMP1 AS(SELECT ac.PERSONID, ac.CLIPID\r\n" + 
			"              FROM ACTEDCHARS achar, ACTED ac\r\n" + 
			"              WHERE achar.ORDERSCREDIT <= 3 AND achar.ACTEDID = ac.ACTEDID),\r\n" + 
			"     TEMP2 AS (SELECT TEMP1.PERSONID, TEMP1.CLIPID, RATINGS.RANK\r\n" + 
			"               FROM TEMP1\r\n" + 
			"               INNER JOIN RATINGS ON TEMP1.CLIPID = RATINGS.CLIPID)\r\n" + 
			"SELECT pp.FULLNAME, tmp_final.AVERAGERATING\r\n" + 
			"FROM(SELECT tmp2.PERSONID, ROUND(AVG(RANK),2) as AVERAGERATING\r\n" + 
			"     FROM TEMP2 tmp2\r\n" + 
			"     GROUP BY tmp2.PERSONID) tmp_final, PEOPLE pp\r\n" + 
			"WHERE tmp_final.PERSONID=pp.PERSONID";
	public static final String qi =	"WITH TEMP1 AS (SELECT GENRE, COUNT(*) count\r\n" + 
			"               FROM GENRES\r\n" + 
			"               GROUP BY GENRE\r\n" + 
			"               ORDER BY COUNT DESC\r\n" + 
			"               FETCH FIRST 1 ROW ONLY)\r\n" + 
			"SELECT ROUND(AVG(rt_clip.RANK),2) AS AVERAGERATING\r\n" + 
			"FROM(SELECT rt.CLIPID, rt.RANK\r\n" + 
			"     FROM TEMP1 tmp1, GENRES gen, RATINGS rt\r\n" + 
			"     WHERE tmp1.GENRE = gen.GENRE AND gen.CLIPID = rt.CLIPID) rt_clip";
	public static final String qj =	"WITH TEMP1 AS (SELECT ac.PERSONID, ac.CLIPID\r\n" + 
			"               FROM(SELECT PERSONID\r\n" + 
			"                    FROM ACTED\r\n" + 
			"                    GROUP BY PERSONID\r\n" + 
			"                    HAVING COUNT(CLIPID) > 100) ac100, ACTED ac\r\n" + 
			"                WHERE ac100.PERSONID = ac.PERSONID),\r\n" + 
			"    TEMP2 AS (SELECT TEMP1.PERSONID, TEMP1.CLIPID, GENRES.GENRE \r\n" + 
			"               FROM TEMP1\r\n" + 
			"               INNER JOIN GENRES ON GENRES.CLIPID = TEMP1.CLIPID),\r\n" + 
			"    TEMP3 AS (SELECT DISTINCT tmp2.PERSONID, tmp2.CLIPID, tmp2.GENRE\r\n" + 
			"               FROM(SELECT PERSONID\r\n" + 
			"                    FROM TEMP2\r\n" + 
			"                    GROUP BY PERSONID\r\n" + 
			"                    HAVING COUNT(CLIPID) > 100) genre100, TEMP2 tmp2\r\n" + 
			"                WHERE genre100.PERSONID = tmp2.PERSONID),\r\n" + 
			"   TEMP4 AS (SELECT PERSONID, CLIPID, LISTAGG(GENRE,' ')\r\n" + 
			"             WITHIN GROUP(ORDER BY GENRE)\r\n" + 
			"             OVER(PARTITION BY PERSONID, CLIPID) AS GENRES\r\n" + 
			"             FROM TEMP3),\r\n" + 
			"   TEMP5 AS (SELECT PERSONID, COUNT(CLIPID) as COUNTSHORT\r\n" + 
			"            FROM(SELECT PERSONID, CLIPID, GENRES\r\n" + 
			"                 FROM TEMP4\r\n" + 
			"                 WHERE TEMP4.GENRES LIKE '%Short%' AND TEMP4.GENRES NOT LIKE '%Drama%' AND TEMP4.GENRES NOT LIKE '%Comedy%')\r\n" + 
			"            GROUP BY PERSONID),\r\n" + 
			"   TEMP6 AS (SELECT PERSONID, COUNT(CLIPID) as COUNTCOMEDY\r\n" + 
			"        FROM(SELECT PERSONID, CLIPID, GENRES\r\n" + 
			"             FROM TEMP4\r\n" + 
			"             WHERE TEMP4.GENRES LIKE '%Comedy%')\r\n" + 
			"        GROUP BY PERSONID),\r\n" + 
			"  TEMP7 AS (SELECT PERSONID, COUNT(CLIPID) as COUNTDRAMA\r\n" + 
			"        FROM(SELECT PERSONID, CLIPID, GENRES\r\n" + 
			"             FROM TEMP4\r\n" + 
			"             WHERE TEMP4.GENRES LIKE '%Drama%')\r\n" + 
			"        GROUP BY PERSONID),\r\n" + 
			"  TEMP8 AS (SELECT PERSONID, COUNT(CLIPID) as COUNTALL\r\n" + 
			"            FROM TEMP4\r\n" + 
			"            GROUP BY PERSONID),\r\n" + 
			"  TEMP9 AS (SELECT TEMP5.PERSONID, TEMP5.COUNTSHORT, TEMP6.COUNTCOMEDY, TEMP7.COUNTDRAMA, TEMP8.COUNTALL\r\n" + 
			"            FROM TEMP5\r\n" + 
			"            INNER JOIN TEMP6 ON TEMP6.PERSONID = TEMP5.PERSONID\r\n" + 
			"            INNER JOIN TEMP7 ON TEMP7.PERSONID = TEMP6.PERSONID\r\n" + 
			"            INNER JOIN TEMP8 ON TEMP8.PERSONID = TEMP7.PERSONID)\r\n" + 
			"SELECT pp.FULLNAME, t9.COUNTDRAMA, t9.COUNTCOMEDY\r\n" + 
			"FROM TEMP9 t9, PEOPLE pp\r\n" + 
			"WHERE t9.COUNTSHORT>=ROUND(0.6*t9.COUNTALL) AND 2*t9.COUNTDRAMA<t9.COUNTCOMEDY AND pp.PERSONID = t9.PERSONID";
	public static final String qk = "WITH TEMP1 AS (SELECT genall.CLIPID\r\n" + 
			"              FROM((SELECT GENRE\r\n" + 
			"                   FROM(SELECT GENRE, COUNT(GENRE) AS COUNT, ROW_NUMBER() OVER(ORDER BY COUNT(GENRE) DESC) AS rn\r\n" + 
			"                        FROM GENRES\r\n" + 
			"                        GROUP BY GENRE)\r\n" + 
			"                   WHERE rn = 2)) gen, GENRES genall\r\n" + 
			"               WHERE gen.GENRE = genall.GENRE)\r\n" + 
			"SELECT COUNT(lang.CLIPID) as COUNTDUTCH\r\n" + 
			"FROM TEMP1 t1, LANGUAGES lang\r\n" + 
			"WHERE t1.CLIPID = lang.CLIPID AND lang.LANGUAGE = 'Dutch'";
	public static final String ql = "WITH TEMP1 AS (SELECT pr.PERSONID, pr.CLIPID\r\n" + 
			"               FROM PRODUCEDROLE prole, PRODUCED pr\r\n" + 
			"               WHERE prole.ROLES LIKE 'coordinating producer%' AND pr.PRODUCEDID = prole.PRODUCEDID),\r\n" + 
			"     TEMP2 AS (SELECT genall.CLIPID\r\n" + 
			"              FROM((SELECT GENRE\r\n" + 
			"                   FROM(SELECT GENRE, COUNT(GENRE) AS COUNT, ROW_NUMBER() OVER(ORDER BY COUNT(GENRE) DESC) AS rn\r\n" + 
			"                        FROM GENRES\r\n" + 
			"                        GROUP BY GENRE)\r\n" + 
			"                   WHERE rn = 1)) gen, GENRES genall\r\n" + 
			"               WHERE gen.GENRE = genall.GENRE),\r\n" + 
			"    TEMP3 AS (SELECT TEMP1.PERSONID, TEMP1.CLIPID\r\n" + 
			"              FROM TEMP1\r\n" + 
			"              INNER JOIN TEMP2 ON TEMP2.CLIPID = TEMP1.CLIPID),\r\n" + 
			"    TEMP4 as (SELECT DISTINCT PERSONID,COUNT(TEMP3.CLIPID) OVER(PARTITION BY TEMP3.PERSONID) as COUNT\r\n" + 
			"              FROM TEMP3\r\n" + 
			"              ORDER BY COUNT DESC)\r\n" + 
			"SELECT pp.FULLNAME, t4.COUNT\r\n" + 
			"FROM TEMP4 t4, PEOPLE pp\r\n" + 
			"WHERE ROWNUM = 1 AND pp.PERSONID = t4.PERSONID";
	
	public static String[] queries2 = {qa, qb, qc, qd, qe, qf , qg , qh, qi, qj, qk , ql};
}
