/**
 * Created by ob0695 on 4/26/2017.
 */
export interface Serializable<T> {
    deserialize(input: Object): T;
}
