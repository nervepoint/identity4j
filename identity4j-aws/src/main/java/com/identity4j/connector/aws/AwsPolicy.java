package com.identity4j.connector.aws;

import java.io.Serializable;
import java.time.Instant;

public class AwsPolicy implements Serializable {
	
	private static final long serialVersionUID = -4125798687167007969L;

	private String policyName;

    private String policyId;

    private String arn;

    private String path;

    private String defaultVersionId;

    private Integer attachmentCount;

    private Integer permissionsBoundaryUsageCount;

    private Boolean isAttachable;

    private String description;

    private Instant createDate;

    private Instant updateDate;

	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public String getPolicyId() {
		return policyId;
	}

	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}

	public String getArn() {
		return arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDefaultVersionId() {
		return defaultVersionId;
	}

	public void setDefaultVersionId(String defaultVersionId) {
		this.defaultVersionId = defaultVersionId;
	}

	public Integer getAttachmentCount() {
		return attachmentCount;
	}

	public void setAttachmentCount(Integer attachmentCount) {
		this.attachmentCount = attachmentCount;
	}

	public Integer getPermissionsBoundaryUsageCount() {
		return permissionsBoundaryUsageCount;
	}

	public void setPermissionsBoundaryUsageCount(Integer permissionsBoundaryUsageCount) {
		this.permissionsBoundaryUsageCount = permissionsBoundaryUsageCount;
	}

	public Boolean getIsAttachable() {
		return isAttachable;
	}

	public void setIsAttachable(Boolean isAttachable) {
		this.isAttachable = isAttachable;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Instant getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Instant createDate) {
		this.createDate = createDate;
	}

	public Instant getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Instant updateDate) {
		this.updateDate = updateDate;
	}

}
