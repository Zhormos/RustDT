/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.tooling.ops;

import melnorme.lang.tooling.data.Severity;
import melnorme.lang.tooling.data.StatusException;
import melnorme.lang.tooling.data.ValidationException;
import melnorme.lang.tooling.ops.AbstractToolOperation;
import melnorme.lang.tooling.ops.IOperationService;
import melnorme.lang.tooling.ops.util.LocationOrSinglePathValidator;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.concurrency.ICancelMonitor;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public abstract class RacerOperation extends AbstractToolOperation {
	
	protected final String racerPath;
	protected final String sdkSrcPath;
	protected final ArrayList2<String> arguments;
	
	protected String input = "";
	
	public RacerOperation(IOperationService opHelper, String racerPath, String sdkSrcPath, 
			ArrayList2<String> arguments) {
		super(opHelper);
		this.racerPath = racerPath;
		this.sdkSrcPath = sdkSrcPath;
		this.arguments = arguments;
	}
	
	protected static ArrayList2<String> getArguments(String opName, int line_0, int col_0, Location fileLocation) {
		ArrayList2<String> arguments = new ArrayList2<String>(opName);
		arguments.add(Integer.toString(line_0 + 1)); // use one-based index
		arguments.add(Integer.toString(col_0)); // But this one is zero-based index
		arguments.add(fileLocation.toPathString());
		return arguments;
	}
	
	protected ProcessBuilder getCommandProcessBuilder() throws ValidationException {
		String toolExePath = new RustRacerLocationValidator().getValidatedPath(racerPath).toString();
		String rustSrcPath = new RustSDKSrcLocationValidator().getValidatedLocation(sdkSrcPath).toString();
		
		ArrayList2<String> cmdLine = new ArrayList2<String>(toolExePath);
		
		cmdLine.addAll(arguments);
		
		ProcessBuilder pb = new ProcessBuilder(cmdLine);
		
		pb.environment().put("RUST_SRC_PATH", rustSrcPath);
		return pb;
	}
	
	public ExternalProcessResult execute(ICancelMonitor cm) throws CommonException, OperationCancellation {
		ProcessBuilder pb = getCommandProcessBuilder();
		return runToolProcess(pb, input, cm);
	}
	
	public static class RustRacerLocationValidator extends LocationOrSinglePathValidator {
		
		public RustRacerLocationValidator() {
			super("Racer executable:");
			fileOnly = true;
		}
		
	}
	
	/* -----------------  ----------------- */
	
	protected RacerCompletionOutputParser createRacerOutputParser(int offset) {
		RacerCompletionOutputParser parser = new RacerCompletionOutputParser(offset) {
			@Override
			protected void handleParseError(CommonException ce) throws CommonException {
				getOperationHelper().logStatus(ce.toStatusException(Severity.WARNING));
			}
			
			@Override
			protected void handleInvalidMatchKindString(String matchKindString) throws CommonException {
				getOperationHelper().logStatus(new StatusException(Severity.WARNING,
					"Unknown Match Kind: " + matchKindString));
			}
		};
		return parser;
	}
	
}