package ve3.os;

import ve3.disassm.V32Disassm.OT;

public class MetaInfo {
	public OT arg1, arg2, arg3, arg4, arg5, arg6;
	public int size;
	
	public MetaInfo() {
		size = 0;
	}
	public MetaInfo(OT arg1) {
		this.arg1 = arg1;
		size = 1;
	}
	public MetaInfo(OT arg1, OT arg2) {
		this(arg1);
		this.arg2 = arg2;
		size = 2;
	}
	public MetaInfo(OT arg1, OT arg2, OT arg3) {
		this(arg1, arg2);
		this.arg3 = arg3;
		size = 3;
	}
	public MetaInfo(OT arg1, OT arg2, OT arg3, OT arg4) {
		this(arg1, arg2, arg3);
		this.arg4 = arg4;
		size = 4;
	}
	public MetaInfo(OT arg1, OT arg2, OT arg3, OT arg4, OT arg5) {
		this(arg1, arg2, arg3, arg4);
		this.arg5 = arg5;
		size = 5;
	}
	public MetaInfo(OT arg1, OT arg2, OT arg3, OT arg4, OT arg5, OT arg6) {
		this(arg1, arg2, arg3, arg4, arg5);
		this.arg6 = arg6;
		size = 6;
	}
}
