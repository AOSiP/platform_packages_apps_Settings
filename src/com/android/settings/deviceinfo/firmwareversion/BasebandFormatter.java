nder the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.firmwareversion;

public class BasebandFormatter {
    public static String getFormattedBaseband(String baseband){
        if (baseband.contains(",")) {
            String[] splitBaseband = baseband.split(",");
            if (splitBaseband.length > 1 && splitBaseband[0].equals(splitBaseband[1])) {
                baseband = splitBaseband[0];
            }
        }
        return baseband;
    }
}
