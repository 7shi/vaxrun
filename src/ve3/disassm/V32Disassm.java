package ve3.disassm;

import java.io.StringWriter;

import ve3.hdw.ConcrateMemory;
import ve3.hdw.Memory;
import ve3.os.OpInfo;
import ve3.os.MetaInfo;
import ve3.os.OpInfo.OpInfoSub;
import ve3.os.OpInfo.Type;

public class V32Disassm {
	
	private Memory memory;
	private OpInfo opinfo;
	private int tsize;
	private StringWriter sw;

	public static enum AdMode {
		Branch, General, PC
	}
	
	/** 
	 * b:byte, w:word, l:long, f:float, df:D_float, Brb:Branch byte, Brw:Branch word
	 * vb:bitfield byte, q:quad
	 * */
	public static enum OT {
		b, w, l, f, q, df, Brb, Brw, vb, 
	}
	
	public enum Ope {

		DUMMY1(0x5bd0, new MetaInfo()), DUMMY2(0x5be0, new MetaInfo()),
		DUMMY3(0x599a, new MetaInfo()), DUMMY4(0x5b98, new MetaInfo()),
		DUMMY5(0xffff, new MetaInfo()), DUMMY6(0xff50, new MetaInfo()),
		DUMMY7(0x5b11, new MetaInfo()), DUMMY8(0x7700, new MetaInfo()),
		DUMMY9(0x5be9, new MetaInfo()), DUMMY10(0x5bde, new MetaInfo()),
		DUMMY11(0x59c3, new MetaInfo()),DUMMY12(0x5ad4, new MetaInfo()),
		DUMMY13(0x5add, new MetaInfo()),DUMMY14(0x5afb, new MetaInfo()),
		DUMMY15(0x5911, new MetaInfo()),DUMMY16(0x5b00, new MetaInfo()),
		DUMMY17(0xfe0d, new MetaInfo()), DUMMY18(0x5bef, new MetaInfo()),		
		DUMMY19(0x5b5a, new MetaInfo()), DUMMY20(0x5bd1, new MetaInfo()),
		DUMMY21(0xffaa, new MetaInfo()),DUMMY22(0x5a11, new MetaInfo()),
		DUMMY23(0xfe00, new MetaInfo()), DUMMY24(0x7732, new MetaInfo()),
		DUMMY25(0x7733, new MetaInfo()), DUMMY26(0x7763, new MetaInfo()),
		DUMMY27(0xfd1f, new MetaInfo()), DUMMY28(0xfe1f, new MetaInfo()),
		DUMMY29(0xff1f, new MetaInfo()), DUMMY30(0xffff, new MetaInfo()),
		DUMMY31(0xfeff, new MetaInfo()), DUMMY32(0xff30, new MetaInfo()),
		DUMMY33(0xff31, new MetaInfo()), DUMMY34(0xff32, new MetaInfo()),
		DUMMY35(0xff33, new MetaInfo()), DUMMY36(0xff3d, new MetaInfo()),
		DUMMY37(0x5700, new MetaInfo()), DUMMY38(0xff27, new MetaInfo()),
		DUMMY39(0xff18, new MetaInfo()), DUMMY40(0xff0a, new MetaInfo()),
		DUMMY41(0xff4d, new MetaInfo()), DUMMY42(0xff13, new MetaInfo()),
		DUMMY43(0xfe0a, new MetaInfo()), DUMMY44(0xffbe, new MetaInfo()),
		DUMMY45(0xffb1, new MetaInfo()), DUMMY46(0xffa4, new MetaInfo()),
		DUMMY47(0xff97, new MetaInfo()), DUMMY48(0xff8a, new MetaInfo()),
		DUMMY49(0xff7d, new MetaInfo()), DUMMY50(0xff70, new MetaInfo()),
		DUMMY51(0xff00, new MetaInfo()), DUMMY52(0xfdff, new MetaInfo()),
		DUMMY53(0xff0f, new MetaInfo()), DUMMY54(0xff2d, new MetaInfo()),
		DUMMY55(0xfff6, new MetaInfo()), DUMMY56(0xfff5, new MetaInfo()),
		DUMMY57(0xfffa, new MetaInfo()), DUMMY58(0xff17, new MetaInfo()),
		DUMMY59(0xfff7, new MetaInfo()), DUMMY60(0xff0e, new MetaInfo()),
		DUMMY61(0x5ad1, new MetaInfo()), DUMMY62(0x5a20, new MetaInfo()),
		
		PUSHL(0xdd, new MetaInfo(OT.l)), 
		PUSHAB(0x9f, new MetaInfo(OT.b)), PUSHAW(0x3f, new MetaInfo(OT.w)),
		PUSHAL(0xdf, new MetaInfo(OT.l)), PUSHAQ(0x7f, new MetaInfo(OT.q)), 
		HALT(0x00, new MetaInfo()), MOVL(0xd0, new MetaInfo(OT.l, OT.l)), 
		TSTL(0xd5, new MetaInfo(OT.l)), NOP(0x01, new MetaInfo()),  
		CHMK(0xbc, new MetaInfo(OT.w)), CHME(0xbd, new MetaInfo(OT.w)), 
		CHMS(0xbe, new MetaInfo(OT.w)), CHMU(0xbf, new MetaInfo(OT.w)), 		
		PROBER(0x0c, new MetaInfo(OT.b, OT.w, OT.b)),PROBEW(0x0d, new MetaInfo(OT.b, OT.w, OT.b)),
		CMPB(0x91, new MetaInfo(OT.b, OT.b)), CMPD(0x71, new MetaInfo(OT.df, OT.df)), 
		CMPF(0x51, new MetaInfo(OT.f, OT.f)), CMPL(0xd1, new MetaInfo(OT.l, OT.l)),
		CMPW(0xb1, new MetaInfo(OT.w, OT.w)), DECB(0x97, new MetaInfo(OT.b)),
		DECL(0xd7, new MetaInfo(OT.l)), ADDL2(0xc0, new MetaInfo(OT.l, OT.l)),
		BRB(0x11, new MetaInfo(OT.Brb)), 		
		CVTLB(0xf6, new MetaInfo(OT.l, OT.b)), CVTBL(0x98, new MetaInfo(OT.b, OT.l)),
		RET(0x4, new MetaInfo()), REMQUE(0x0f, new MetaInfo(OT.b, OT.l)),
		XBICW3(0xab, new MetaInfo(OT.w, OT.w, OT.w)), 
		BBC(0xe1, new MetaInfo(OT.l, OT.vb, OT.Brb)), 
		CVTWL(0x32, new MetaInfo(OT.w, OT.l)),	JMP(0x17, new MetaInfo(OT.b)),		
		REMQHI(0x5e, new MetaInfo(OT.q, OT.l)), BICL2(0xca, new MetaInfo(OT.l, OT.l)),
		MOVB(0x90, new MetaInfo(OT.b, OT.b)), 
		BLBC(0xe9, new MetaInfo(OT.l, OT.Brb)), BLBS(0xe8, new MetaInfo(OT.l, OT.Brb)),
		DIVD3(0x67, new MetaInfo(OT.df, OT.df, OT.df)), DIVB3(0x87, new MetaInfo(OT.b, OT.b, OT.b)),
		DIVW3(0xa7, new MetaInfo(OT.w, OT.w, OT.w)), DIVL3(0xc7, new MetaInfo(OT.l, OT.l, OT.l)),				
		CVTWB(0x33, new MetaInfo(OT.w, OT.b)), CVTPL(0x36, new MetaInfo(OT.w, OT.b, OT.l)),		
		BICB3(0x8b, new MetaInfo(OT.b, OT.b, OT.b)), MOVF(0x50, new MetaInfo(OT.f, OT.f)),
		CVTWF(0x4d, new MetaInfo(OT.w, OT.f)), POPR(0xba, new MetaInfo(OT.w)),
		MOVTC(0x2e, new MetaInfo(OT.w, OT.b, OT.b, OT.w, OT.b, OT.b)),
		CVTDF(0x76, new MetaInfo(OT.df, OT.f)), 
		BRW(0x31, new MetaInfo(OT.Brw)), 
		MOVC3(0x28, new MetaInfo(OT.w, OT.b, OT.b)), CVTBD(0x6c, new MetaInfo(OT.b, OT.df)),
		BBS(0xe0, new MetaInfo(OT.l, OT.vb, OT.Brb)), LOCC(0x3a, new MetaInfo(OT.b, OT.w, OT.b)),		
		SKPC(0x3b, new MetaInfo(OT.b, OT.w, OT.b)), 
		CVTLP(0xf9, new MetaInfo(OT.l, OT.w, OT.b)), EDITPC(0x38, new MetaInfo(OT.w, OT.b, OT.b, OT.b)),		
		CVTLD(0x6e, new MetaInfo(OT.l, OT.df)), INDEX(0xa, new MetaInfo(OT.l, OT.l, OT.l, OT.l, OT.l, OT.l)),
		DIVF3(0x47, new MetaInfo(OT.f, OT.f, OT.f)), INSQUE(0x0e, new MetaInfo(OT.b, OT.b)),
		CVTPS(0x08, new MetaInfo(OT.w, OT.b, OT.w, OT.b)), 
		BICB2(0x8a, new MetaInfo(OT.b, OT.b)), BICW2(0xaa, new MetaInfo(OT.w, OT.w)), 
		TSTB(0x95, new MetaInfo(OT.b)),
		ASHL(0x78, new MetaInfo(OT.b, OT.l, OT.l)), ASHQ(0x79, new MetaInfo(OT.b, OT.q, OT.q)), 
		BICL3(0xcb, new MetaInfo(OT.l, OT.l, OT.l)), 
		DECW(0xb7, new MetaInfo(OT.w)), TSTW(0xb5, new MetaInfo(OT.w)),
		CVTLW(0xf7, new MetaInfo(OT.l, OT.w)), BPT(0x3, new MetaInfo()),
		MOVTUC(0x2f, new MetaInfo(OT.w, OT.b, OT.b, OT.b, OT.w, OT.b)),
		ADAWI(0x58, new MetaInfo(OT.w, OT.w)), CVTDW(0x69, new MetaInfo(OT.df, OT.w)),
		ADDP4(0x20, new MetaInfo(OT.w, OT.b, OT.w, OT.b)), 
		ADDP6(0x21, new MetaInfo(OT.w, OT.b, OT.w, OT.b, OT.w, OT.b)), 
		TSTD(0x73, new MetaInfo(OT.df)), MOVD(0x70, new MetaInfo(OT.df, OT.df)),
		CVTFW(0x49, new MetaInfo(OT.f, OT.w)),
		CVTBF(0x4c, new MetaInfo(OT.b, OT.f)), TSTF(0x53, new MetaInfo(OT.f)),
		CVTWD(0x6d, new MetaInfo(OT.w, OT.df)), RSB(0x5, new MetaInfo()),
		POLYF(0x55, new MetaInfo(OT.f, OT.w, OT.b)), POLYD(0x75, new MetaInfo(OT.df, OT.w, OT.b)),
		ADDF2(0x40, new MetaInfo(OT.f, OT.f)), REI(0x2, new MetaInfo()),
		CMPC3(0x29, new MetaInfo(OT.w, OT.b, OT.b)), CMPC5(0x2d, new MetaInfo(OT.w, OT.b, OT.b, OT.w, OT.b)),
		CMPP3(0x35, new MetaInfo(OT.w, OT.b, OT.b)), CMPP4(0x37, new MetaInfo(OT.w, OT.b, OT.w, OT.b)),
		LDPCTX(0x6, new MetaInfo()), SVPCTX(0x7, new MetaInfo()),
		MOVC5(0x2c, new MetaInfo(OT.w, OT.b, OT.b, OT.w, OT.b)),
		SUBB2(0x82, new MetaInfo(OT.b, OT.b)), SUBW2(0xa2, new MetaInfo(OT.w, OT.w)),
		SUBL2(0xc2, new MetaInfo(OT.l, OT.l)), SUBF2(0x42, new MetaInfo(OT.f, OT.f)),
		SUBD2(0x62, new MetaInfo(OT.df, OT.df)), MULP(0x25, new MetaInfo(OT.w, OT.b, OT.w, OT.b, OT.w, OT.b)),
		MOVAB(0x9e,new MetaInfo(OT.b, OT.l)), MOVAW(0x3e,new MetaInfo(OT.w, OT.l)),
		MOVAL(0xde,new MetaInfo(OT.l, OT.l)), MOVAQ(0x7e,new MetaInfo(OT.q, OT.l)),
		XORB2(0x8c, new MetaInfo(OT.b, OT.b)), XORW2(0xac, new MetaInfo(OT.w, OT.w)), XORL2(0xcc, new MetaInfo(OT.l, OT.l)),
		XORB3(0x8d, new MetaInfo(OT.b, OT.b, OT.b)), XORW3(0xad, new MetaInfo(OT.w, OT.w, OT.w)), XORL3(0xcd, new MetaInfo(OT.l, OT.l, OT.l)),
		SOBGEQ(0xf4, new MetaInfo(OT.l, OT.Brb)), SOBGTR(0xf5, new MetaInfo(OT.l, OT.Brb)),
		ASHP(0xf8, new MetaInfo(OT.b, OT.w, OT.b, OT.b, OT.w, OT.b)),
		CMPV(0xec, new MetaInfo(OT.l, OT.b, OT.b, OT.l)), CMPZV(0xed, new MetaInfo(OT.l, OT.b, OT.b, OT.l)),
		AOBLSS(0xf2, new MetaInfo(OT.l, OT.l, OT.Brb)), AOBLEQ(0xf3, new MetaInfo(OT.l, OT.l, OT.Brb)),
		BSBB(0x10, new MetaInfo(OT.Brb)), BSBW(0x30, new MetaInfo(OT.Brw)),		
		MULB2(0x84, new MetaInfo(OT.b, OT.b)), MULB3(0x85, new MetaInfo(OT.b, OT.b, OT.b)), 
		MULW2(0xa4, new MetaInfo(OT.w, OT.w)), MULW3(0xa5, new MetaInfo(OT.w, OT.w, OT.w)), 
		MULL2(0xc4, new MetaInfo(OT.l, OT.l)), MULL3(0xc5, new MetaInfo(OT.l, OT.l, OT.l)), 
		MULF2(0x44, new MetaInfo(OT.f, OT.f)), MULF3(0x45, new MetaInfo(OT.f, OT.f, OT.f)), 
		MULD2(0x64, new MetaInfo(OT.df, OT.df)), MULD3(0x65, new MetaInfo(OT.df, OT.df, OT.df)),
		MCOMB(0x92, new MetaInfo(OT.b, OT.b)), MCOMW(0xb2, new MetaInfo(OT.w, OT.w)), MCOML(0xd2, new MetaInfo(OT.l, OT.l)),
		BISB2(0x88, new MetaInfo(OT.b, OT.b)), BISB3(0x89, new MetaInfo(OT.b, OT.b, OT.b)),
		BISW2(0xa8, new MetaInfo(OT.w, OT.w)), BISW3(0xa9, new MetaInfo(OT.w, OT.w, OT.w)),
		BISL2(0xc8, new MetaInfo(OT.l, OT.l)), BISL3(0xc9, new MetaInfo(OT.l, OT.l, OT.l)),
		BISPSW(0xb8, new MetaInfo(OT.w)), BICPSW(0xb9, new MetaInfo(OT.w)),
		CASEB(0x8f, new MetaInfo(OT.b, OT.b, OT.b)), CASEW(0xaf, new MetaInfo(OT.w, OT.w, OT.w)),
		CASEL(0xcf, new MetaInfo(OT.l, OT.l, OT.l)), CVTRFL(0x4b, new MetaInfo(OT.f, OT.l)),
		CVTDL(0x6a, new MetaInfo(OT.df, OT.l)), INCB(0x96, new MetaInfo(OT.b)),
		INCW(0xb6, new MetaInfo(OT.w)), INCL(0xd6, new MetaInfo(OT.l)),
		MOVP(0x34, new MetaInfo(OT.w, OT.b, OT.b)),
		BBSS(0xe2, new MetaInfo(OT.l, OT.b, OT.Brb)), BBCS(0xe3, new MetaInfo(OT.l, OT.b, OT.Brb)),
		BBSC(0xe4, new MetaInfo(OT.l, OT.b, OT.Brb)), BBCC(0xe5, new MetaInfo(OT.l, OT.b, OT.Brb)),
		CVTLF(0x4e, new MetaInfo(OT.l, OT.f)), CVTFB(0x48, new MetaInfo(OT.f, OT.b)),
		BITB(0x93, new MetaInfo(OT.b, OT.b)), BITW(0xb3, new MetaInfo(OT.w, OT.w)), BITL(0xd3, new MetaInfo(OT.l, OT.l)),		
		BNEQ(0x12, new MetaInfo(OT.Brb)), BEQL(0x13, new MetaInfo(OT.Brb)), BGTR(0x14, new MetaInfo(OT.Brb)),
		BLEQ(0x15, new MetaInfo(OT.Brb)), BGEQ(0x18, new MetaInfo(OT.Brb)), BLSS(0x19, new MetaInfo(OT.Brb)),
		BGTRU(0x1a, new MetaInfo(OT.Brb)), BLEQU(0x1b, new MetaInfo(OT.Brb)),BVC(0x1c, new MetaInfo(OT.Brb)), 
		BVS(0x1d, new MetaInfo(OT.Brb)), BCC(0x1e, new MetaInfo(OT.Brb)), BLSSU(0x1f, new MetaInfo(OT.Brb)),		
		ACBB(0x9d, new MetaInfo(OT.b, OT.b, OT.b, OT.Brw)), ACBW(0x3d, new MetaInfo(OT.w, OT.w, OT.w, OT.Brw)),
		ACBL(0xf1, new MetaInfo(OT.l, OT.l, OT.l, OT.Brw)), ACBD(0x6f, new MetaInfo(OT.df, OT.df, OT.df, OT.Brw)),
		ACBF(0x4f, new MetaInfo(OT.f, OT.f, OT.f, OT.Brw)),
		CLRB(0x94, new MetaInfo(OT.b)), CLRW(0xb4, new MetaInfo(OT.w)), 
		CLRD(0x7c, new MetaInfo(OT.df)), CLRF(0xd4, new MetaInfo(OT.f)),
		SUBB3(0x83, new MetaInfo(OT.b, OT.b, OT.b)), SUBW3(0xa3, new MetaInfo(OT.w, OT.w, OT.w)),
		SUBL3(0xc3, new MetaInfo(OT.l, OT.l, OT.l)), SUBF3(0x43, new MetaInfo(OT.f, OT.f, OT.f)),
		SUBD3(0x63, new MetaInfo(OT.df, OT.df, OT.df)), ROTL(0x9c, new MetaInfo(OT.b, OT.l, OT.l)),
		ENUL(0x7a, new MetaInfo(OT.l, OT.l, OT.l, OT.q)), INSQHI(0x5c, new MetaInfo(OT.b, OT.q)), 
		MOVW(0xb0, new MetaInfo(OT.w, OT.w)), MOVQ(0x7d, new MetaInfo(OT.q, OT.q)),
		SCANC(0x2a, new MetaInfo(OT.w, OT.b, OT.b, OT.b)), SPANC(0x2b, new MetaInfo(OT.w, OT.b, OT.b, OT.b)),
		CALLG(0xfa, new MetaInfo(OT.b, OT.b)), CALLS(0xfb, new MetaInfo(OT.l, OT.b)),
		CVTSP(0x9, new MetaInfo(OT.w, OT.b, OT.w, OT.b)), CRC(0xb, new MetaInfo(OT.b, OT.l, OT.w, OT.b)),
		CVTBW(0x99, new MetaInfo(OT.b, OT.w)), ADDD2(0x60, new MetaInfo(OT.df, OT.df)),
		ADDB2(0x80, new MetaInfo(OT.b, OT.b)), ADDW2(0xa0, new MetaInfo(OT.w, OT.w)),
		MNEGB(0x8e, new MetaInfo(OT.b, OT.b)), MNEGW(0xae, new MetaInfo(OT.w, OT.w)), 
		MNEGL(0xce, new MetaInfo(OT.l, OT.l)), MNEGF(0x52, new MetaInfo(OT.f, OT.f)),
		MNEGD(0x72, new MetaInfo(OT.df, OT.df)), SUBP4(0x22, new MetaInfo(OT.w, OT.b, OT.w, OT.b)),		
		SUBP6(0x23, new MetaInfo(OT.w, OT.b, OT.w, OT.b, OT.w, OT.b)),
		CVTPT(0x24, new MetaInfo(OT.w, OT.b, OT.b, OT.w, OT.b)), CVTTP(0x26, new MetaInfo(OT.w, OT.b, OT.b, OT.w, OT.b)),
		DIVP(0x27, new MetaInfo(OT.w, OT.b, OT.w, OT.b, OT.w, OT.b)),
		ADDB3(0x81, new MetaInfo(OT.b, OT.b, OT.b)), ADDW3(0xa1, new MetaInfo(OT.w, OT.w, OT.w)),
		ADDL3(0xc1, new MetaInfo(OT.l, OT.l, OT.l)),  ADDF3(0x41, new MetaInfo(OT.f, OT.f, OT.f)), 
		ADDD3(0x61, new MetaInfo(OT.df, OT.df, OT.df)),
		ADWC(0xd8, new MetaInfo(OT.l, OT.l)), CVTDB(0x68, new MetaInfo(OT.df, OT.b)),
		BBSSI(0xe6, new MetaInfo(OT.l, OT.b, OT.b)), BBCCI(0xe7, new MetaInfo(OT.l, OT.b, OT.b)),
		CVTFD(0x56, new MetaInfo(OT.f, OT.df)), CVTFL(0x4a, new MetaInfo(OT.f, OT.l)),
		CVTRDL(0x6b, new MetaInfo(OT.df, OT.l)),
		DIVB2(0x86, new MetaInfo(OT.b, OT.b)), DIVW2(0xa6, new MetaInfo(OT.w, OT.w)), 
		DIVL2(0xc6, new MetaInfo(OT.l, OT.l)), DIVF2(0x46, new MetaInfo(OT.f, OT.f)), 
		DIVD2(0x66, new MetaInfo(OT.df, OT.df)),
		EDIV(0x7b, new MetaInfo(OT.l, OT.q, OT.l, OT.l)),
		EMODG(0x54, new MetaInfo(OT.f, OT.b, OT.f, OT.l, OT.f)), EMODD(0x74, new MetaInfo(OT.df, OT.b, OT.df, OT.l, OT.df)),
		EXTV(0xee, new MetaInfo(OT.l, OT.b, OT.vb, OT.l)), EXTZV(0xef, new MetaInfo(OT.l, OT.b, OT.vb, OT.l)),
		FFC(0xeb, new MetaInfo(OT.l, OT.b, OT.b, OT.l)), FFS(0xea, new MetaInfo(OT.l, OT.b, OT.b, OT.l)),
		INSV(0xf0, new MetaInfo(OT.l, OT.l, OT.b, OT.b)), JSB(0x16, new MetaInfo(OT.b)),
		MFPR(0xdb, new MetaInfo(OT.l, OT.l)), MOVPSL(0xdc, new MetaInfo(OT.l)),
		MOVZBW(0x9b, new MetaInfo(OT.b, OT.w)), MOVZBL(0x9a, new MetaInfo(OT.b, OT.l)), 
		MOVZWL(0x3c, new MetaInfo(OT.w, OT.l)), MTPR(0xda, new MetaInfo(OT.l, OT.l)),
		PUSHR(0xbb, new MetaInfo(OT.w)), SBWC(0xd9, new MetaInfo(OT.l, OT.l)),
		XFC(0xfc, new MetaInfo()), MATCHC(0x39, new MetaInfo(OT.w, OT.b, OT.w, OT.b));
		                                        
		public final int mne;		
		public final String opname;
		public final MetaInfo minfo;
		public static Ope[] table = new Ope[0xfffff];
		
		static {
			for (Ope ope : Ope.values()) {
				table[ope.mne] = ope;
			}
		}		
		
		Ope(int mne, MetaInfo minfo) {
			this.mne = mne;
			this.minfo = minfo;
			this.opname = toString().toLowerCase();
		}
	}
	
	public V32Disassm() {
		memory = new ConcrateMemory(0xfffff);
		opinfo = new OpInfo();
		sw = new StringWriter();
	}
	
	public V32Disassm(byte[] rawdata) {
		this();
		tsize = readInt(rawdata, 4);
		System.out.printf("textsize = 0x%x\n", tsize);
		memory.load(rawdata, 0x20, tsize);
		//memory.dump();
	}
	
		
	private int readInt(byte[] rawdata, int offset) {
		return rawdata[offset] & 0xff | (rawdata[offset+1] & 0xff) << 8
				| (rawdata[offset+2] & 0xff) << 16 | (rawdata[offset+3] & 0xff) << 24;
	}
	
	private long fetch(OT optype) {
		switch(optype) {
		case df:
		case q: {
			return memory.fetch8().lval;
		}
		case f:
		case l: {
			return memory.fetch4().ival;			
		}
		case w: {
			return memory.fetch2().sval;
		}
		case b: 
		case vb: {			
			return memory.fetch().bval;
		}
		default: {
			System.out.println("unrecognised optype(OT) in fetch");
			System.exit(1);
		}
		}
		
		return 0;
		
	}
	
	private OpInfoSub resolveDispPc(OT optype, byte type) {
		switch(type) {
		case 0x8: { // Immediate
			opinfo.opsub.type = Type.Immed;
			opinfo.opsub.arg = fetch(optype);
			return opinfo.opsub;
		}
		case 0x9: { // Absolute
			opinfo.opsub.type = Type.Abs;
			opinfo.opsub.arg = memory.fetch4().ival;
			return opinfo.opsub;
		}
		case 0xc: { // word relative
			opinfo.opsub.type = Type.WordRel;
			opinfo.opsub.arg = memory.fetch2().sval + memory.getCurrentPc();
			return opinfo.opsub;
		}
		case 0xd: { // word relative deferred
			opinfo.opsub.type = Type.WordRelDefer;
			opinfo.opsub.arg = memory.fetch2().sval + memory.getCurrentPc();
			return opinfo.opsub;
		}
		case 0xe: { // long relative
			opinfo.opsub.type = Type.LongRel;
			opinfo.opsub.arg = memory.fetch4().ival + memory.getCurrentPc();
			return opinfo.opsub;
		}
		case 0xf: { // long relative deferred
			opinfo.opsub.type = Type.LongRelDefer;
			opinfo.opsub.arg = memory.fetch4().ival + memory.getCurrentPc();
			return opinfo.opsub;
		}
		default: {
			System.out.println("unsupported byte in program couter address mode in resolveDispPc: " + type);
			System.exit(1);
		}
		}		
		return null;		
	}
	
	private OpInfoSub resolveDisp(OT optype) {
		if (optype == OT.Brb) {
			opinfo.opsub.type = Type.Branch1;
			opinfo.opsub.arg = memory.fetch().bval + memory.getCurrentPc();
			//System.out.printf("disp = 0x%x\n", opinfo.opsub.arg);
			return opinfo.opsub;
		} else if(optype == OT.Brw) {
			opinfo.opsub.type = Type.Branch2;
			opinfo.opsub.arg = memory.fetch2().sval + memory.getCurrentPc();
			return opinfo.opsub;
		}
		
		byte arg = memory.fetch().bval;
		byte type = (byte)((arg >> 4) & 0xf);
		byte value = (byte)(arg & 0xf);
		
		//opinfo.opsub.admode = OpInfo.AddressMode.General;
		switch (type) {
		case 0:
		case 1:
		case 2:
		case 3: { // immediate data
			opinfo.opsub.type = Type.Literal;
			opinfo.opsub.operand = (byte)(arg & 0x3f);
			return opinfo.opsub;				
		}
		case 4: { // index
			opinfo.opsub.type = Type.Index;
			opinfo.opsub.operand = (byte)(arg & 0x3f);
			return opinfo.opsub;
		}
		case 5: { // Register
			opinfo.opsub.type = Type.Register;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			return opinfo.opsub;				
		}
		case 6: { // Register Defered 
			opinfo.opsub.type = Type.RegDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);	
			return opinfo.opsub;
		}
		case 7: { // Auto Decrement
			opinfo.opsub.type = Type.AutoDec;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			return opinfo.opsub;		
		}
		case 8: { // Auto Increment
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.AutoInc;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			return opinfo.opsub;				
		}
		case 9: {
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.AutoIncDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			return opinfo.opsub;			
		}
		case 0xa: { // Byte Displacement
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.ByteDisp;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = memory.fetch().bval;
			return opinfo.opsub;				
		}
		case 0xb: { // Byte Displacement Deferred
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.ByteDispDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = memory.fetch().bval;
			return opinfo.opsub;
		}
		case 0x0c: { // Word Displacement
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.WordDisp;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = memory.fetch2().sval;
			return opinfo.opsub;
		}
		case 0x0d: { // Word Displacement Deferred
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.WordDispDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = memory.fetch2().sval;
			return opinfo.opsub;
		}
		case 0x0e: { // Long Displacement
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.LongDisp;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = memory.fetch4().ival;
			return opinfo.opsub;
		}
		case 0x0f: { // Long Displacement Deferred
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.LongDispDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = memory.fetch4().ival;
			return opinfo.opsub;
		}
		default: { // index
			System.out.printf("%x is not implemented yet in resolveDisp\n", type);
			System.exit(1);				
			break;
		}
		
		}			
		
		return null;
	}
	
	private void setArg1(MetaInfo minfo) {
		OpInfoSub opsub = null;
		while ((opsub = resolveDisp(minfo.arg1)).type == Type.Index) {
			opinfo.pushIdx1(opsub.operand);
		}		
		opinfo.setType1(opsub.type);
		opinfo.setOpe1(opsub.operand);
		opinfo.setArg1(opsub.arg);
	}
	private void setArg2(MetaInfo minfo) {
		setArg1(minfo);		
		OpInfoSub opsub = null;
		while ((opsub = resolveDisp(minfo.arg2)).type == Type.Index) {
			opinfo.pushIdx2(opsub.operand);
		}		
		opinfo.setType2(opsub.type);
		opinfo.setOpe2(opsub.operand);
		opinfo.setArg2(opsub.arg);
	}
	
	private void setArg3(MetaInfo minfo) {
		setArg2(minfo);
		OpInfoSub opsub;
		while ((opsub = resolveDisp(minfo.arg3)).type == Type.Index) {
			opinfo.pushIdx3(opsub.operand);
		}
		
		opinfo.setType3(opsub.type);
		opinfo.setOpe3(opsub.operand);
		opinfo.setArg3(opsub.arg);
	}
	
	private void setArg4(MetaInfo minfo) {
		setArg3(minfo);
		OpInfoSub opsub = null;
		while ((opsub = resolveDisp(minfo.arg4)).type == Type.Index) {
			opinfo.pushIdx4(opsub.operand);
		}
		
		opinfo.setType4(opsub.type);
		opinfo.setOpe4(opsub.operand);
		opinfo.setArg4(opsub.arg);
	}
	
	private void setArg5(MetaInfo minfo) {
		setArg4(minfo);
		OpInfoSub opsub;		
		while((opsub = resolveDisp(minfo.arg5)).type == Type.Index) {
			opinfo.pushIdx5(opsub.operand);
		}
		
		opinfo.setType5(opsub.type);
		opinfo.setOpe5(opsub.operand);
		opinfo.setArg5(opsub.arg);		
	}
	
	private void setArg6(MetaInfo minfo) {
		setArg5(minfo);
		OpInfoSub opsub;
		while ((opsub = resolveDisp(minfo.arg6)).type == Type.Index) {
			opinfo.pushIdx6(opsub.operand);
		}
		
		opinfo.setType6(opsub.type);
		opinfo.setOpe6(opsub.operand);
		opinfo.setArg6(opsub.arg);		
	}
	
	
	
	private String format(int index, byte[] rawdata, String s) {
		//return String.format("%4x:   %s    %s", index, new String(rawdata), s);
		return String.format("%4x:\t%s\t%s", index, new String(rawdata), s);
	}
	private String format(int index, byte[] rawdata) {
		//return String.format("%4x:   %s", index, new String(rawdata));	
		return String.format("%4x:\t%s", index, new String(rawdata));	
	}
	
	
	private void showlog(String s) {
		System.out.println(s);
		sw.write(s + "\r\n");
		while(memory.remaining()) {
			String log = format(memory.getPrevPc(), memory.rawdump_rem());
			sw.write(log + "\r\n");
			System.out.println(log);
		}
	}
	
	public String disassm() {
		while (true) {
			opinfo.clear();
			run();
			if (memory.getCurrentPc() > tsize) break;			
		}
		return sw.toString();
	}
	
	private void run() {
		int index = memory.savePc();
		//int b1 = (opinfo.setOpCode(memory.fetch())) & 0xff;
		int b1 = opinfo.setOpCode(memory.fetch().bval & 0xff);
		Ope ope = Ope.table[b1];

		if (ope == null) {
			b1 = opinfo.setOpCode(b1 << 8 | memory.fetch().bval & 0xff);			
			ope = Ope.table[b1];
			if (ope == null) {
				System.out.printf("0x%x unrecognised mnemonic in run1\n", b1);
				System.exit(1);
			} else {
				// temporary
				showlog(format(index, memory.rawdump(), ".word " + String.format("0x%x", b1)));
				return;
			}
		}


		opinfo.minfo = ope.minfo;
		switch(ope.minfo.size) {
		case 0: {
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		case 1: {
			setArg1(ope.minfo);
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		case 2: {			
			setArg2(ope.minfo);
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));			
			break;
		}
		case 3: {
			setArg3(ope.minfo);
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		case 4: {
			setArg4(ope.minfo);
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		case 5: {
			setArg5(ope.minfo);
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		case 6: {
			setArg6(ope.minfo);
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		default: {
			System.out.printf("0x%x unrecognised size in run\n", ope.minfo.size);
			System.exit(1);
		}
		}		
	}
		
}
