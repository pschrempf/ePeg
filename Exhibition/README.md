EPeg Exhibition
================================================================================
This README will describe every aspect of the ePeg Exhibition project,
from how to set up a new set, to detailing what sort of communications take place and the various data formats.

Setting up the Raspberry Pi 3 B+
--------------------------------------------------------------------------------


Socket Communications
--------------------------------------------------------------------------------
All communications take place using the JSON data format.

Currently, there are two different messages that can be sent from the server to the frontend:

``player_status`` messages to alert the frontend that a player's status has changed, e.g. they connected
or disconnected. Their format is as follows:

```javascript
{
id :: string // unique identifier for the tablet. Note that this shouldn't be the socket.io socket id, as it changes at every reconnection. It should be something like a MAC-address, or something hard-coded.
}
```

The other set of messages are called ``player_action`` and they are __always__ relayed by the server straight from the tablets. They describe __intents__ from the player on the given tablet, such as starting a new game, or moving onto the next trial. Their format is as follows:

```javascript
{
sender_id :: string                      // same uid as the 'id' field in the 'player_status' messages
action_type :: [ NEW_SINGLE_GAME  = 0    // Request a new single player game
               | NEW_MULTI_GAME   = 1    // Request a new multi player game
               | START_NEXT_TRIAL = 2    // Request that the tablet start a new trial
               | TRIAL_FINISHED   = 3    // Let us know that 
               ]
action_data :: JSON Object               // Contains additional data about the action
}
```
