package com.identity4j.connector.principal;

import java.io.Serializable;
import java.util.Date;

public class AccountStatus implements Serializable {

	private static final long serialVersionUID = 3599159041356306662L;

	private Date expire;
	private Date locked;
	private Date unlocked;
	private AccountStatusType type;
	private boolean disabled;

	/**
	 * Constructor for connectors that do not support password status in any
	 * way.
	 */
	public AccountStatus() {
		this.type = AccountStatusType.unlocked;
	}

	/**
	 * Get if the account is disabled.
	 * 
	 * @return disabled
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * Set if the account is disabled.
	 * 
	 * @param disabled disabled
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
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
	 * Set the date the account expires. Set to <code>null</code> if the
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
	 * Get the status type.
	 * 
	 * @return status type
	 */
	public AccountStatusType getType() {
		return type;
	}

	/**
	 * Set the status type.
	 * 
	 * @param type status type
	 */
	public void setType(AccountStatusType type) {
		this.type = type;
	}

	/**
	 * Get the date when the password became unlocked. This is only relevant if
	 * the type is {@link PasswordStatusType#unlocked}.
	 * 
	 * @return locked
	 */
	public final Date getUnlocked() {
		return unlocked;
	}

	/**
	 * Set the date when the password will became unlocked.
	 * 
	 * @param unlocked unlocked date
	 */
	public final void setUnlocked(Date unlocked) {
		this.unlocked = unlocked;
	}

	/**
	 * Get the date when the password became locked. This is only relevant if
	 * the type is {@link PasswordStatusType#locked}.
	 * 
	 * @return locked
	 */
	public final Date getLocked() {
		return locked;
	}

	/**
	 * Set the date when the password became locked.
	 * 
	 * @param unlocked unlocked date
	 */
	public final void setLocked(Date locked) {
		this.locked = locked;
	}

	/**
	 * Calculate that status type based on the dates attributes set in this
	 * object.
	 */
	public void calculateType() {
		Date now = new Date();
		setType(AccountStatusType.unlocked);
		if(isDisabled()) {
			setType(AccountStatusType.disabled);
		}
		else if (getExpire() != null && now.after(getExpire())) {
			// Expired account
			setType(AccountStatusType.expired);
		} else if (getLocked() != null) {
			if (getUnlocked() == null || getUnlocked().compareTo(now) < 1) {
				setType(AccountStatusType.locked);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[type='").append(getType());
		builder.append("', expire='").append(getExpire() == null ? "" : getExpire().toString());
		builder.append("', locked='").append(getLocked() == null ? "" : getLocked().toString());
		builder.append("', unlocked='").append(getUnlocked() == null ? "" : getUnlocked().toString());
		builder.append("']");
		return builder.toString();
	}

	public void lock() {
		setType(AccountStatusType.locked);
		setLocked(new Date());
	}

	public void unlock() {
		setType(AccountStatusType.unlocked);
		setUnlocked(new Date());
	}
}