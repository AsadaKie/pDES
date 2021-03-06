/*
 * Copyright (c) 2016, Design Engineering Laboratory, The University of Tokyo.
 * All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package org.pdes.rcp.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.pdes.rcp.actions.base.AbstractSimulationAction;
import org.pdes.rcp.model.ProjectDiagram;
import org.pdes.simulator.PDES_BasicSimulator_TaskPerformedBySingleTaskWorkers;
import org.pdes.simulator.model.ProjectInfo;
import org.pdes.simulator.model.base.BaseProjectInfo;

/**
 * This is the Action class for running PDES_BasicSimulator_TaskPerformedBySingleTaskWorkers at once.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class OneRunPDES_BasicSimulator_TaskPerformedBySingleTaskWorkersAction extends AbstractSimulationAction {
	
	private final String text = "Basic DES (a task performed by single task workers)";
	
	public OneRunPDES_BasicSimulator_TaskPerformedBySingleTaskWorkersAction(){
		this.setToolTipText(text);
		this.setText(text);
	}
	
	/* (non-Javadoc)
	 * @see org.pdes.rcp.actions.base.AbstractOneRunSimulationAction#doSimulation()
	 */
	@Override
	protected List<Future<String>> doSimulation(ProjectDiagram diagram, int workflowCount) {
		long start = System.currentTimeMillis();
		ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Future<String>> resultList = new ArrayList<Future<String>>();
		resultList.add(service.submit(new BasicSimulationTask(0, diagram, workflowCount, outputDir)));
		service.shutdown();
		long end = System.currentTimeMillis();
		msgStream.println("Processing time: " + ((end - start)) + " [millisec]");
		return resultList;
	}
	
	/**
	 * This is the concurrent callable class for doing simulation by another thread.<br>
	 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
	 */
	private class BasicSimulationTask implements Callable<String>{
		
		private final int no;
		private final ProjectDiagram diagram;
		private final int numOfWorkflow;
		private final String outputDirectoryPath;
		
		/**
		 * This is the constructor.
		 * @param no
		 * @param diagram
		 * @param numOfWorkflow
		 */
		public BasicSimulationTask(int no, ProjectDiagram diagram, int numOfWorkflow, String outputDirectoryPath) {
			this.no = no;
			this.diagram = diagram;
			this.numOfWorkflow = numOfWorkflow;
			this.outputDirectoryPath = outputDirectoryPath;
		}

		/* (non-Javadoc)
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public String call() throws Exception {
			BaseProjectInfo project = new ProjectInfo(diagram, numOfWorkflow);
			PDES_BasicSimulator_TaskPerformedBySingleTaskWorkers sim = new PDES_BasicSimulator_TaskPerformedBySingleTaskWorkers(project);
			sim.execute();
			sim.saveResultFilesInDirectory(outputDirectoryPath, String.valueOf(no));
			return String.format("%d,%f,%d,%f", no, project.getTotalCost(), project.getDuration(),project.getTotalActualWorkAmount());
		}
	}

}
