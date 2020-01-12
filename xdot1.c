#include "dot_util.h"
#include "RadioEvent.h"
#if ACTIVE_EXAMPLE == OTA_EXAMPLE

#define MQ2_thre_up 9138
#define MQ2_thre_down 8343.254
#define MQ7_thre_up 16108.088
#define MQ7_thre_down 15612.856
#define DHT11_thre_up 23207.272
#define DHT11_thre_down 23007.656


        
/////////////////////////////////////////////////////////////////////////////
// -------------------- DOT LIBRARY REQUIRED ------------------------------//
// * Because these example programs can be used for both mDot and xDot     //
//     devices, the LoRa stack is not included. The libmDot library should //
//     be imported if building for mDot devices. The libxDot library       //
//     should be imported if building for xDot devices.                    //
// * https://developer.mbed.org/teams/MultiTech/code/libmDot-dev-mbed5/    //
// * https://developer.mbed.org/teams/MultiTech/code/libmDot-mbed5/        //
// * https://developer.mbed.org/teams/MultiTech/code/libxDot-dev-mbed5/    //
// * https://developer.mbed.org/teams/MultiTech/code/libxDot-mbed5/        //
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////
// * these options must match the settings on your gateway //
// * edit their values to match your configuration         //
// * frequency sub band is only relevant for the 915 bands //
// * either the network name and passphrase can be used or //
//     the network ID (8 bytes) and KEY (16 bytes)         //
/////////////////////////////////////////////////////////////
static std::string network_name = "ee542-conduit";
static std::string network_passphrase = "ee542-conduit";
//static uint8_t network_id[] = { 0x6C, 0x4E, 0xEF, 0x66, 0xF4, 0x79, 0x86, 0xA6 };
//static uint8_t network_key[] = { 0x1F, 0x33, 0xA1, 0x70, 0xA5, 0xF1, 0xFD, 0xA0, 0xAB, 0x69, 0x7A, 0xAE, 0x2B, 0x95, 0x91, 0x6B };
static uint8_t frequency_sub_band = 7;
static lora::NetworkType network_type = lora::PUBLIC_LORAWAN;
static uint8_t join_delay = 5;
static uint8_t ack = 0;
static bool adr = true;

// deepsleep consumes slightly less current than sleep
// in sleep mode, IO state is maintained, RAM is retained, and application will resume after waking up
// in deepsleep mode, IOs float, RAM is lost, and application will start from beginning after waking up
// if deep_sleep == true, device will enter deepsleep mode
static bool deep_sleep = false;

mDot* dot = NULL;
lora::ChannelPlan* plan = NULL;
bool result;
Serial pc(USBTX, USBRX);

#if defined(TARGET_XDOT_L151CC)
I2C i2c(I2C_SDA, I2C_SCL);
ISL29011 lux(i2c);
#else
AnalogIn lux(XBEE_AD0);
#endif
AnalogIn DHT11(GPIO0);
AnalogIn MQ2(GPIO1);
AnalogIn MQ7(GPIO2);
DigitalOut vcc(GPIO3);
//DigitalOut light(GPIO)


int main() {
    // Custom event handler for automatically displaying RX data
    RadioEvent events;

    pc.baud(115200);

#if defined(TARGET_XDOT_L151CC)
    i2c.frequency(400000);
#endif

    mts::MTSLog::setLogLevel(mts::MTSLog::TRACE_LEVEL);
    
#if CHANNEL_PLAN == CP_US915
    plan = new lora::ChannelPlan_US915();
#elif CHANNEL_PLAN == CP_AU915
    plan = new lora::ChannelPlan_AU915();
#elif CHANNEL_PLAN == CP_EU868
    plan = new lora::ChannelPlan_EU868();
#elif CHANNEL_PLAN == CP_KR920
    plan = new lora::ChannelPlan_KR920();
#elif CHANNEL_PLAN == CP_AS923
    plan = new lora::ChannelPlan_AS923();
#elif CHANNEL_PLAN == CP_AS923_JAPAN
    plan = new lora::ChannelPlan_AS923_Japan();
#elif CHANNEL_PLAN == CP_IN865
    plan = new lora::ChannelPlan_IN865();
#endif
    assert(plan);

    dot = mDot::getInstance(plan);
    assert(dot);
    logInfo("Enter main function.");
    // attach the custom events handler
    dot->setEvents(&events);

    if (!dot->getStandbyFlag()) {
        logInfo("mbed-os library version: %d.%d.%d", MBED_MAJOR_VERSION, MBED_MINOR_VERSION, MBED_PATCH_VERSION);

        // start from a well-known state
        logInfo("defaulting Dot configuration");
        dot->resetConfig();
        dot->resetNetworkSession();

        // make sure library logging is turned on
        dot->setLogLevel(mts::MTSLog::INFO_LEVEL);

        // update configuration if necessary
        if (dot->getJoinMode() != mDot::OTA) {
            logInfo("changing network join mode to OTA");
            if (dot->setJoinMode(mDot::OTA) != mDot::MDOT_OK) {
                logError("failed to set network join mode to OTA");
            }
        }
        // in OTA and AUTO_OTA join modes, the credentials can be passed to the library as a name and passphrase or an ID and KEY
        // only one method or the other should be used!
        // network ID = crc64(network name)
        // network KEY = cmac(network passphrase)
        update_ota_config_name_phrase(network_name, network_passphrase, frequency_sub_band, network_type, ack);
        //update_ota_config_id_key(network_id, network_key, frequency_sub_band, network_type, ack);

        // configure network link checks
        // network link checks are a good alternative to requiring the gateway to ACK every packet and should allow a single gateway to handle more Dots
        // check the link every count packets
        // declare the Dot disconnected after threshold failed link checks
        // for count = 3 and threshold = 5, the Dot will ask for a link check response every 5 packets and will consider the connection lost if it fails to receive 3 responses in a row
        update_network_link_check_config(3, 5);

        // enable or disable Adaptive Data Rate
        dot->setAdr(adr);

        // Configure the join delay
        dot->setJoinDelay(join_delay);

        // save changes to configuration
        logInfo("saving configuration");
        if (!dot->saveConfig()) {
            logError("failed to save configuration");
        }

        // display configuration
        display_config();
    } else {
        // restore the saved session if the dot woke from deepsleep mode
        // useful to use with deepsleep because session info is otherwise lost when the dot enters deepsleep
        logInfo("restoring network session from NVM");
        dot->restoreNetworkSession();
    }
    
    int MQ2_value, MQ7_value, DHT11_value;
    std::vector<uint8_t> tx_data;
    int32_t receive;

    
    while (true) {

        // join network if not joined
        if (!dot->getNetworkJoinStatus()) {
            join_network();
        }
        

#if defined(TARGET_XDOT_L151CC)
        //light = 0;
        vcc = 1;
        MQ2_value = int(MQ2.read_u16());
        MQ7_value = int(MQ7.read_u16());
        DHT11_value = int(DHT11.read_u16());

        while(MQ2_value > MQ2_thre_up || MQ2_value < MQ2_thre_down|| MQ7_value > MQ7_thre_up || MQ2_value < MQ2_thre_down|| DHT11_value > DHT11_thre_up || DHT11_value < DHT11_thre_down){
            
            //light = 1; //TODO
            logInfo("Alarm is triggered");

            tx_data.push_back((MQ2_value >> 8) & 0xFF);
            tx_data.push_back(MQ2_value & 0xFF);
            logInfo("MQ2: %lu [0x%04X]", MQ2_value, MQ2_value);
            // send_data(tx_data);
            // tx_data.clear();sensor1
            
            tx_data.push_back((MQ7_value >> 8) & 0xFF);
            tx_data.push_back(MQ7_value & 0xFF);
            logInfo("MQ7: %lu [0x%04X]", MQ7_value, MQ7_value);
            // send_data(tx_data);
            // tx_data.clear();
            
            tx_data.push_back((DHT11_value >> 8) & 0xFF);
            tx_data.push_back(DHT11_value & 0xFF);
            logInfo("DHT11: %lu [0x%04X]", DHT11_value, DHT11_value);
            send_data(tx_data);
            tx_data.clear();

            MQ2_value = int(MQ2.read_u16());
            MQ7_value = int(MQ7.read_u16());
            DHT11_value = int(DHT11.read_u16());

    
            //= std::equal(receive.begin(), receive.end(), dot-> getDeviceId().begin());
            // if (result){
            //     logInfo("Alarm is cancelled");
            //     tx_data.clear();
            //     break;
            // }
            result = true;
            receive = dot->recv(tx_data);
           
          //  logInfo("device ID");
//            for (int i=0;i<dot-> getDeviceId().size();i++)
//                logInfo("%u",dot-> getDeviceId()[i]);
//            logInfo("%u",dot->getDeviceId().size());
            
            if (!receive){

               // logInfo("received");
//                for (int i=0;i<tx_data.size();i++)
//                    logInfo("%u",tx_data[i]);
//                logInfo("%u",tx_data.size());
                for (int i=0;i<tx_data.size();i++){
                    if (dot-> getDeviceId()[i] != tx_data[i]){
                        result = false;
                        break;
                    }     
                }
                tx_data.clear();
                logInfo("boolean %b", result);
                if (result){
                    logInfo("Alarm is cancelled");
                    break;
                }
            }
            wait(1);
        }

        // // Receive data first
        // receive = dot->recv(tx_data);
        // for (int i=0;i<tx_data.size();i++)
        //     logInfo("%u",tx_data[i]);
        // logInfo("\n");
        // tx_data.clear();
        wait(1);
        
#endif

        // if going into deepsleep mode, save the session so we don't need to join again after waking up
        // not necessary if going into sleep mode since RAM is retained
        if (deep_sleep) {
            logInfo("saving network session to NVM");
            dot->saveNetworkSession();
        }

        // ONLY ONE of the three functions below should be uncommented depending on the desired wakeup method
        //sleep_wake_rtc_only(deep_sleep);
        //sleep_wake_interrupt_only(deep_sleep);
        sleep_wake_rtc_or_interrupt(deep_sleep,10);
    }
 
    return 0;
}

#endif
