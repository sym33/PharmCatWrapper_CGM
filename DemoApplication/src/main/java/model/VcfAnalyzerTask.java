package model;

import java.io.File;

import ca.uhn.hl7v2.model.v24.message.ORM_O01;




public class VcfAnalyzerTask {
	
	String key;
	ORM_O01 orderMessage;
	File vcf;
	File order;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public ORM_O01 getOrderMessage() {
		return orderMessage;
	}
	public void setOrderMessage(ORM_O01 orderMessage) {
		this.orderMessage = orderMessage;
	}
	public File getVcf() {
		return vcf;
	}
	public void setVcf(File vcf) {
		this.vcf = vcf;
	}
	public File getOrder() {
		return order;
	}
	public void setOrder(File order) {
		this.order = order;
	}
}
