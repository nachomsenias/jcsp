package jcsp.util;

import jcsp.CSPSolution;

import org.jamesframework.core.search.Search;
import org.jamesframework.core.search.listeners.SearchListener;

@SuppressWarnings("rawtypes")
public class ProgressSearchListener implements SearchListener<CSPSolution> {

    public void searchStarted( Search search) {
        System.out.println(" >>> Search started");
    }

    public void searchStopped(Search search) {
        System.out.println(" >>> Search stopped (" 
        		+ search.getRuntime()/1000 + " sec, " 
        		+ search.getSteps() + "  steps)");
    }
}