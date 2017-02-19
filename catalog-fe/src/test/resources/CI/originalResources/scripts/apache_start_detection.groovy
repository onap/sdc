import org.cloudifysource.dsl.utils.ServiceUtils

println "apache_start_detection.groovy: port http=${PORT} ..."
def isPortOccupied = ServiceUtils.isPortOccupied(Integer.parseInt(PORT))
println "apache_start_detection.groovy: isPortOccupied http=${PORT} ... ${isPortOccupied}"
return isPortOccupied
