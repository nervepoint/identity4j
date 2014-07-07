package com.identity4j.connector.principal;

import java.io.Serializable;
import java.util.Date;

public class PasswordStatus  implements Serializable {

	private static final long serialVersionUID = -4370203404346470398L;

	/**
	 * Default password status if the connector does not support password status
	 */
	private Date expire;
	private Date lastChange;
	private Date unlocked;
	private PasswordStatusType type;
	private Date warn;
	private Date disable;
	private boolean needChange;

	/**
	 * Constructor for connectors that do not support password status in any
	 * way.
	 */
	public PasswordStatus() {
		this(null, null, PasswordStatusType.upToDate);
	}

	/**
	 * Constructor.
	 * 
	 * @param lastChange password last changed date
	 * @param expire expire date
	 * @param type type
	 */
	public PasswordStatus(Date lastChange, Date expire, PasswordStatusType type) {
		this.type = type;
		this.lastChange = lastChange;
		this.expire = expire;
	}

	public boolean isNeedChange() {
		return needChange;
	}

	public void setNeedChange(boolean needChange) {
		this.needChange = needChange;
	}

	/**
	 * If the connector supports password changing, it may return a date the
	 * password was last changed. Todays date should be returned if this
	 * information is not available so it is assumed the password does not need
	 * changing.
	 * 
	 * @return last password change date
	 */
	public Date getLastChange() {
		return lastChange;
	}

	/**
	 * Set the last password change date.
	 * 
	 * @param lastChange password change date
	 */
	public void setLastChange(Date lastChange) {
		this.lastChange = lastChange;
	}

	/**
	 * Get the date the password expires. Will be <code>null</code> if the
	 * connector does not support password expire. If the password has not yet
	 * expired, this will contain the date expiry will occur, otherwise it will
	 * contain the date the password expired.
	 * 
	 * @return password expire date
	 */
	public Date getExpire() {
		return expire;
	}

	/**
	 * Set the date the password expires. Set to <code>null</code> if the
	 * connector does not support password expire. If the password has already
	 * expired, set to the expired date, otherwise set it to the date the
	 * password expires.
	 * 
	 * @param expire expire date
	 */
	public void setExpire(Date expire) {
		this.expire = expire;
	}

	/**
	 * Set the date the identity will be warned their password will expiry. Will
	 * be <code>null</code> if the connector does not support password expire.
	 * 
	 * @param warn password warn date
	 */
	public final void setWarn(Date warn) {
		this.warn = warn;
	}

	/**
	 * Get the date the identity will be warned their password will expiry. Will
	 * be <code>null</code> if the connector does not support password expire.
	 * 
	 * @return password warn date
	 */
	public final Date getWarn() {
		return warn;
	}

	/**
	 * Get the status type.
	 * 
	 * @return status type
	 */
	public PasswordStatusType getType() {
		return type;
	}

	/**
	 * Set the status type.
	 * 
	 * @param type status type
	 */
	public void setType(PasswordStatusType type) {
		this.type = type;
	}

	/**
	 * Get the date the password will be disabled if the password is not
	 * changed. Will be <code>null</code> if the connector does not support
	 * password expire or password disabling. If the password has not yet been
	 * disabled, this will contain the date disabling will occur, otherwise it
	 * will contain the date the password was disabled.
	 * 
	 * @return password disable date
	 */
	public final Date getDisable() {
		return disable;
	}

	/**
	 * Set the date the password will be disabled if the password is not
	 * changed. Will be <code>null</code> if the connector does not support
	 * password expire or password disabling. If the password has not yet been
	 * disabled, this will contain the date disabling will occur, otherwise it
	 * will contain the date the password was disabled.
	 * 
	 * @param disable password disable date
	 */
	public final void setDisable(Date disable) {
		this.disable = disable;
	}

	/**
	 * Get the date when the password becomes unlocked. This is only relevant if
	 * the type is {@link PasswordStatusType#locked}.
	 * 
	 * @return locked
	 */
	public final Date getUnlocked() {
		return unlocked;
	}

	/**
	 * Set the date when the password becomes unlocked. This is only relevant if
	 * the type is {@link PasswordStatusType#locked}.
	 * 
	 * @param unlocked unlocked date
	 */
	public final void setUnlocked(Date unlocked) {
		this.unlocked = unlocked;
	}

	/**
	 * Calculate that status type based on the dates attribtues set in this
	 * object.
	 */
	public void calculateType() {
		Date now = new Date();
		if(isNeedChange()) {
			setType(PasswordStatusType.changeRequired);
		}
		else if (getExpire() != null && now.after(getExpire())) {
			// Expired, must change password
			setType(PasswordStatusType.expired);
		} else if (getWarn() != null && now.after(getWarn())) {
			// Near expiry
			setType(PasswordStatusType.nearExpiry);
		} else {
			// Locked
			// TODO prevent password change if locked. Can be
			// permanently locked as well by making unlock > expiry
			if (getUnlocked() != null && now.before(getUnlocked())) {
				setType(PasswordStatusType.locked);
			} else {
				setType(PasswordStatusType.upToDate);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[type='").append(getType());
		builder.append("', expire='").append(getExpire() == null ? "" : getExpire().toString());
		builder.append("', lastChange='").append(getLastChange() == null ? "" : getLastChange().toString());
		builder.append("', unlocked='").append(getUnlocked() == null ? "" : getUnlocked().toString());
		builder.append("', disable='").append(getDisable() == null ? "" : getDisable().toString());
		builder.append("']");
		return builder.toString();
	}
}