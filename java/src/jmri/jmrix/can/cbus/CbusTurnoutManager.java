package jmri.jmrix.can.cbus;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Turnout;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CAN CBUS implementation of a TurnoutManager.
 * <p>
 * Turnouts must be manually created.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @since 2.3.1
 */
public class CbusTurnoutManager extends AbstractTurnoutManager {

    public CbusTurnoutManager(CanSystemConnectionMemo memo) {
        this.memo = memo;
        prefix = memo.getSystemPrefix();
    }

    CanSystemConnectionMemo memo;

    String prefix = "M";

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    /** 
     * {@inheritDoc} 
     * Overriden to normalize System Name
     */
    @Override
    public Turnout provideTurnout(@Nonnull String key) {
        String name = normalizeSystemName(key);
        Turnout result = getTurnout(name);
        if (result == null) {
            if (name.startsWith(getSystemPrefix() + typeLetter())) {
                result = newTurnout(name, null);
            } else {
                result = newTurnout(makeSystemName(name), null);
            }
        }
        return result;
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    protected Turnout createNewTurnout(String systemName, String userName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        // first, check validity
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            throw e;
        }
        String newAddress = CbusAddress.validateSysName(addr);
        Turnout t = new CbusTurnout(getSystemPrefix(), newAddress, memo.getTrafficController());
        t.setUserName(userName);
        return t;
    }

    /** 
     * {@inheritDoc} 
     * systemName M
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        // first, check validity
        try {
            validateSystemNameFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        // prefix + as service to user
        String newAddress = CbusAddress.validateSysName(curAddress);
        return getSystemPrefix() + typeLetter() + newAddress;
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public String getNextValidAddress(String curAddress, String prefix) throws JmriException {
        String testAddr = curAddress;
        // make sure starting name is valid
        try {
            validateSystemNameFormat(testAddr);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        //If the hardware address passed does not already exist then this can
        //be considered the next valid address.
        Turnout s = getBySystemName(prefix + typeLetter() + testAddr);
        if (s == null) {
            return testAddr;
        }
        // build local addresses
        String newaddr = CbusAddress.getIncrement(testAddr);
        if (newaddr==null) {
            return null;
        }
        //If the new hardware address does not already exist then this can
        //be considered the next valid address.
        Turnout snew = getBySystemName(prefix + typeLetter() + newaddr);
        if (snew == null) {
            return newaddr;
        }
        return null;
    }
    
    /** 
     * {@inheritDoc} 
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        String addr;
        try {
            addr = systemName.substring(getSystemPrefix().length() + 1); // get only the address part
        } catch (StringIndexOutOfBoundsException e){
            return NameValidity.INVALID;
        }
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e){
            return NameValidity.INVALID;
        }
        return NameValidity.VALID;
    }

    /**
     * Work out the details for Cbus hardware address validation
     * Logging of handled cases no higher than WARN.
     *
     * @param address the hardware address to check
     * @throws IllegalArgumentException when delimiter is not found
     */
    void validateSystemNameFormat(String address) throws IllegalArgumentException {
        String newAddress = CbusAddress.validateSysName(address);
        log.debug("validated system name {}",newAddress);
    }


    /** {@inheritDoc} */
    @Override
    public Turnout getBySystemName(@Nonnull String key) {
        String name = normalizeSystemName(key);
        return _tsys.get(name);
    }

    /**
     * {@inheritDoc}
     * 
     * Forces upper case and trims leading and trailing whitespace.
     * Does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException.
     */
    @CheckReturnValue
    @Override
    public @Nonnull
    String normalizeSystemName(@Nonnull String inputName) {
        // does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException
        return inputName.toUpperCase().trim();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddOutputEntryToolTip");
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(CbusTurnoutManager.class);

}
