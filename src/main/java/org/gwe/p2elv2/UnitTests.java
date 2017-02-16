/*
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gwe.p2elv2;

import java.util.List;

import org.gwe.p2elv2.functions.PFConst;
import org.gwe.p2elv2.functions.PFCount;
import org.gwe.p2elv2.functions.PFDir;
import org.gwe.p2elv2.functions.PFFileDownload;
import org.gwe.p2elv2.functions.PFFileUpload;
import org.gwe.p2elv2.functions.PFRange;
import org.gwe.p2elv2.functions.PFXCat;
import org.gwe.p2elv2.model.PStatement;
import org.gwe.utils.rex.REXParser;
import org.gwe.utils.rex.config.REXInspector4Class;

/**
 * @author Marco Ruiz
 * @since Jul 30, 2008
 */
// Just in case the reader is asking why using unit tests in this fashion is cause the tests must check contents of real
// file systems to do the dirs and such. In the future I'll create mock file systems to create standard unit tests 
// against them, in the mean time this will do.
class UnitTests {
	
	public static void main(String[] args) throws Exception {
		System.out.println(REXInspector4Class.getConfig(PStatement.class).getPattern());
		String p2el;
		
		String rex = "(\\s*\\$\\{\\s*([a-zA-Z]\\w*[.]?){1,}\\s*=\\s*(\\$[a-zA-Z]\\w*\\s*\\(([^\\(\\),]*[,]?){1,}\\)|[^\\s]*)\\s*){0,}";
		
		p2el = "${Var1}=const(${Var2},alfa) ${Var2}=count(${Var4}) ${Var4}=const(1,8,3) template";
		p2el = "${Var2}=count(${Var4.DIM1}${Var4.DIM2}) ${Var4(DIM1|DIM2)}=const(0|1,1|0,0|4) template";
		p2el = "${ Var1 } = const ( a , b , c d )  ${Var2} = count( ${Var4.DIM1} ) ${  Var4 ( DIM1 | DIM2 ) } = const ( 1|2 , 5|10 , 3|6 ) ${Var1}-${Var2}-${Var4.DIM1}-${Var4.DIM2}";
		
		p2el = "${Var1}=$const(1,2,3,4) ${filename}=out([.]log|-${Var1}*) ${Files}=$dir(sftp://birn-cluster0.nbirn.net/home/mruiz/,p2elDirTest,${Var1}*,${filename}) ${Var2}=$count(${Var1}) ${Var3}=$const(${Var2}); ${Var1}-${Var2}-${Var3}: ${Files}";
		p2el = "${Var1}=$const(a,b,c) ${file.rex}=out([.]log|-\\d*) ${Files}=$dir(sftp://birn-cluster0.nbirn.net/home/mruiz/,p2elDirTest,${Var1.id}*,${file.rex}) ${Var2}=$count(${Var1.id}) ${Var1.id}=$count(5) ${file.rex}-${Var1}-${Var1.id}-${Var2}-${Var3}: ${Files}";

		p2el = " ${Var1.id}=$count(5) ${Var1.name}=$const(1,2) ${file.rex}=out([.]log|-(${Var1.id}*|\\d*)) " +
				"${remoteDir}=sftp://birn-cluster0.nbirn.net/home/mruiz ${remoteFile}=$dir(${remoteDir},p2elDirTest,${Var1.id}*,${file.rex}) " +
				"${remoteFileLocalCopy}=$in(${remoteFile}) ${newRemoteFile}=$out(${remoteDir}/${remoteFileLocalCopy}) " +
				"cp ${remoteFileLocalCopy} ${newRemoteFile}";
		
//		PStatement stmt = REXParser.createModel(PStatement.class, 
//		"${Var1}=const(${Var2},alfa) ${Var2}=const(file,path,dir) ${Var3}=const(${Var1},BBB,${Var4}) ${Var4}=const(1,2,3) ${Var5}=const(AAA,${Var3},CCC) template");
//		PStatement stmt = REXParser.createModel(PStatement.class, "${myArray(1)}=const(a,b,c) ${FILE}=expand(file, path, now) ${HIST}=range(20,100,010) ${Array01(2)}=const(1,2,3) ${function}=myfunction(params) ${SAM}=range(500,5000,0500) ${ITER}=range(10,50,10) /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/Slicer3 --launch /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/lib/Slicer3/Plugins/BSplineDeformableRegistration --iterations ${ITER} --gridSize 5 --histogrambins ${HIST} --spatialsamples ${SAM} --maximumDeformation 1 --default 0 --resampledmovingfilename /export2/mruiz/slicer-test/out-${ITER}-${HIST}-${SAM}.nrrd in[fixed.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/fixed.nrrd?view=co in[moving.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/moving.nrrd?view=co");
//		PStatement stmt = REXParser.createModel(PStatement.class, "${myArray(1,2,3)}=[(a,b,c),(4,5,6),(7,8,9)] ${FILE}=expand(file, path, now) ${HIST}=[20..100||010] ${Array01(1,2,3)}=[(1,2,3),(4,5,6),(7,8,9)] ${function(x,y)}=myfunction(params) ${SAM}=[500..5000||0500] ${ITER}=[10..50||10] /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/Slicer3 --launch /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/lib/Slicer3/Plugins/BSplineDeformableRegistration --iterations ${ITER} --gridSize 5 --histogrambins ${HIST} --spatialsamples ${SAM} --maximumDeformation 1 --default 0 --resampledmovingfilename /export2/mruiz/slicer-test/out-${ITER}-${HIST}-${SAM}.nrrd in[fixed.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/fixed.nrrd?view=co in[moving.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/moving.nrrd?view=co");
//		REXParser.createModel(PStatement.class, "${function(x,y)}=f:myfunction(params) ${ITER}=f:expand(file, path, now) /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/Slicer3 --launch /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/lib/Slicer3/Plugins/BSplineDeformableRegistration --iterations ${ITER} --gridSize 5 --histogrambins ${HIST} --spatialsamples ${SAM} --maximumDeformation 1 --default 0 --resampledmovingfilename /export2/mruiz/slicer-test/out-${ITER}-${HIST}-${SAM}.nrrd in[fixed.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/fixed.nrrd?view=co in[moving.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/moving.nrrd?view=co");
//		REXParser.createModel(PStatement.class, "${function(x,y)}=f:myfunction(params) ${ITER}=f:expand(file, path, now) /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/Slicer3 --launch /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/lib/Slicer3/Plugins/BSplineDeformableRegistration --iterations ${ITER} --gridSize 5 --histogrambins ${HIST} --spatialsamples ${SAM} --maximumDeformation 1 --default 0 --resampledmovingfilename /export2/mruiz/slicer-test/out-${ITER}-${HIST}-${SAM}.nrrd in[fixed.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/fixed.nrrd?view=co in[moving.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/moving.nrrd?view=co");
	
		p2el = "${FILES}=$xcat(/Users/admin/work/eclipse-ws/gwe-core/src/temp/test.xcat) echo '${FILES} ${FILES.ID} ${FILES.URI} ${SYSTEM.JOB_ID} ${SYSTEM.JOB_NUM}'";
		
		p2el = "${SLICER_HOME}= ${INDEX}=$range(0,50,01) ${FASTA_FILE}=$in(http://www.expasy.org/uniprot/P801${INDEX}.fas) cat ${FASTA_FILE}";
		
		p2el = "${INDEX}=$range(0,50,5) ${NUM}=${SYSTEM.JOB_NUM} echo ${NUM}";
		
		PStatement stmt = REXParser.createModel(PStatement.class, p2el);
		System.out.println(stmt);

		new PFConst();
		new PFRange();
		new PFCount();
		new PFDir();
		new PFFileDownload();
		new PFFileUpload();
		new PFXCat();

//		ClientConfig appConfig = new XMLClientConfigReader(null, null).getClientConfiguration();
//		PStatementContext ctx = new PStatementContext("", appConfig.getKeys(), null);
		PStatementContext ctx = new PStatementContext("", null, null);
		ctx.addSystemVar("JOB_ID", "Identifier");
		ctx.addSystemVar("JOB_NUM", "Number");
		
		PStatementCompiler compiler = new PStatementCompiler(stmt, ctx);
//		PStatementCompiler compiler = new PStatementCompiler(stmt, null, null);
		
		List<PPermutation> perms = compiler.compile();
		System.out.println(perms);
		
		System.out.println(compiler.createStatements(perms));

		for (PPermutation perm : perms) perm.processRuntimedependencies(ctx);
		System.out.println(perms);
		
		System.out.println(compiler.createStatements(perms));
	}
}

