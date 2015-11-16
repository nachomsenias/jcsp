package jcsp.experiment.beans;

import java.io.File;

import jcsp.CSPProblem;
import jcsp.algo.Algorithm;
import util.io.ConfigFileReader;

public abstract class AlgorithmBean {

	protected abstract void readConfigFile(ConfigFileReader reader);
	
	public abstract Algorithm createAlgorithmInstance(CSPProblem csp, boolean verbose);
	
	public static AlgorithmBean getBean(File configFile) 
			throws InstantiationException, IllegalAccessException, 
				ClassNotFoundException {
		
		ConfigFileReader reader = new ConfigFileReader();
		reader.readConfigFile(configFile);
		
		String algorithm = reader.getParameterString("algorithm");
		
		String beanClassName = "jcsp.experiment.beans."+algorithm+"Bean";
		
		AlgorithmBean bean = 
				(AlgorithmBean)Class.forName(beanClassName).newInstance();
		
		bean.readConfigFile(reader);
		
		return bean;
	}
}
