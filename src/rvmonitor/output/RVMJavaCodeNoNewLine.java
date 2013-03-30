package rvmonitor.output;

import rvmonitor.parser.ast.mopspec.PropertyAndHandlers;

public class RVMJavaCodeNoNewLine extends RVMJavaCode {

	public RVMJavaCodeNoNewLine(String code) {
		super(code);
	}

	public RVMJavaCodeNoNewLine(String code, RVMVariable monitorName) {
		super(code, monitorName);
	}

	public RVMJavaCodeNoNewLine(PropertyAndHandlers prop, String code, RVMVariable monitorName) {
		super(prop, code, monitorName);
	}

	public String toString() {
		String ret = super.toString();
		ret = ret.trim();

		if (ret.length() != 0 && ret.endsWith("\n"))
			ret = ret.substring(0, ret.length() - 1);

		return ret;
	}

}