from sdcBePy.consumers.models.consumerCandidate import ConsumerCandidate

consumersList = ["aai", "appc", "dcae", "mso", "sdnc", "vid", "cognita",
                 "clamp", "vfc", "workflow", "policy", "pomba",
                 "multicloud", "cds", "modeling"]
salt = "9cd4c3ad2a6f6ce3f3414e68b5157e63"
password = "35371c046f88c603ccba152cb3db34ec4475cb2e5713f2fc0a43bf18a5243495"


def get_consumers():
    return [ConsumerCandidate(name, slat=salt, password=password) for name in consumersList]
