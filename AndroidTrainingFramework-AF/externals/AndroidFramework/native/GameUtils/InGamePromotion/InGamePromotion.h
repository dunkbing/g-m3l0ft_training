#ifndef _ACP_IN_GAME_PROMOTION_H_
#define _ACP_IN_GAME_PROMOTION_H_

/* Local includes */
#include "../helpers.h"

/* Library includes */

/* Standard includes */
#include <map>
#include <string>

#include <jni.h>


namespace acp_utils 
{
    namespace modules
    {
        /**
         * InGamePromotion class is a collection of static functions,
         * all related to the promotion features,
         * which require using the IGPFreemiumActivity java class.
         */
		 
        class InGamePromotion
        {
        public:
            /**
             * Show the freemium In Game Promotion screen
             *
			 * @param language:  the language used when showing the IGP.
			 * @param gamecode:  the 4-letter GGC_GAME_CODE.
             */
            static bool LaunchIGP(acp_utils::helpers::Language language, bool portrait = false);
			
            /**
             * Retrieve the reward items
             *
			 * @param language:  the language used when showing the IGP.
			 * @param gamecode:  the 4-letter GGC_GAME_CODE.
             */
			static void RetrieveItems(acp_utils::helpers::Language language, const char* gamecode);
			
        private:
			static void SetPromotionClass();
			static jclass s_PromotionClass;
		};
	}
}
#endif // _ACP_IN_GAME_PROMOTION_H_