package link.angelmaker.bitexchanger;

/**
 * An extension to the BitExchanger, an exchanger that has roles specified.
 * Generally Master should be the one initiating things (sending)
 * While Slave follows.
 * @author I3anaan
 *
 */
public interface MasterSlaveBitExchanger extends BitExchanger {

	public boolean isMaster();
	public boolean isSlave();
}
