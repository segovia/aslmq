package shared;


public interface DatabaseMonitor {

	public void setStatementExecTime(Long statementExecTime);

	public void setStatementExecTimeToUnknown();
}
