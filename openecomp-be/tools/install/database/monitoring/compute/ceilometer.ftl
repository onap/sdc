{
    "ceilometerMetricList": [
        {
            "name": "instance",
            "type": "Gauge",
            "unit": "instance",
            "category": "compute",
            "description": "Existence of instance"
        },
        {
            "name": "memory",
            "type": "Gauge",
            "unit": "MB",
            "category": "compute",
            "description": "Volume of RAM allocated to the instance"
        },
        {
            "name": "memory.usage",
            "type": "Gauge",
            "unit": "MB",
            "category": "compute",
            "description": "Volume of RAM used by the instance from the amount of its allocated memory"
        },
        {
            "name": "memory.resident",
            "type": "Gauge",
            "unit": "MB",
            "category": "compute",
            "description": "Volume of RAM used by the instance on the physical machine"
        },
        {
            "name": "cpu",
            "type": "Cumulative",
            "unit": "ns",
            "category": "compute",
            "description": "CPU time used"
        },
        {
            "name": "cpu_util",
            "type": "Gauge",
            "unit": "%",
            "category": "compute",
            "description": "Average CPU utilization"
        },
        {
            "name": "cpu.delta",
            "type": "Delta",
            "unit": "ns",
            "category": "compute",
            "description": "CPU time used since previous datapoint"
        },
        {
            "name": "vcpus",
            "type": "Gauge",
            "unit": "vcpu",
            "category": "compute",
            "description": "Number of virtual CPUs allocated to the instance"
        },
        {
            "name": "disk.latency",
            "type": "Gauge",
            "unit": "ms",
            "category": "disk",
            "description": "Average disk latency"
        },
        {
            "name": "disk.iops",
            "type": "Gauge",
            "unit": "count/s",
            "category": "disk",
            "description": "Average disk iops"
        },
        {
            "name": "disk.device.latency",
            "type": "Gauge",
            "unit": "ms",
            "category": "disk",
            "description": "Average disk latency per device"
        },
        {
            "name": "disk.device.iops",
            "type": "Gauge",
            "unit": "count/s",
            "category": "disk",
            "description": "Average disk iops per device"
        },
        {
            "name": "disk.capacity",
            "type": "Gauge",
            "unit": "B",
            "category": "disk",
            "description": "The amount of disk that the instance can see"
        },
        {
            "name": "disk.allocation",
            "type": "Gauge",
            "unit": "B",
            "category": "disk",
            "description": "The amount of disk occupied by the instance on the host machine"
        },
        {
            "name": "disk.usage",
            "type": "Gauge",
            "unit": "B",
            "category": "disk",
            "description": "The physical size in bytes of the image container on the host"
        },
        {
            "name": "disk.device.capacity",
            "type": "Gauge",
            "unit": "B",
            "category": "disk",
            "description": "The amount of disk per device that the instance can see"
        },
        {
            "name": "disk.device.allocation",
            "type": "Gauge",
            "unit": "B",
            "category": "disk",
            "description": "The amount of disk per device occupied by the instance on the host machine"
        },
        {
            "name": "disk.device.usage",
            "type": "Gauge",
            "unit": "B",
            "category": "disk",
            "description": "The physical size in bytes of the image container on the host per device"
        },
        {
            "name": "disk.device.read.requests",
            "type": "Cumulative",
            "unit": "request",
            "category": "disk",
            "description": "Number of read requests"
        },
        {
            "name": "disk.device.read.requests.rate",
            "type": "Gauge",
            "unit": "request/s",
            "category": "disk",
            "description": "Average rate of read requests"
        },
        {
            "name": "disk.device.write.requests",
            "type": "Cumulative",
            "unit": "request",
            "category": "disk",
            "description": "Number of write requests"
        },
        {
            "name": "disk.device.write.requests.rate",
            "type": "Gauge",
            "unit": "request/s",
            "category": "disk",
            "description": "Average rate of write requests"
        },
        {
            "name": "disk.device.read.bytes",
            "type": "Cumulative",
            "unit": "B",
            "category": "disk",
            "description": "Volume of reads"
        },
        {
            "name": "disk.device.read.bytes.rate",
            "type": "Gauge",
            "unit": "B/s",
            "category": "disk",
            "description": "Average rate of reads"
        },
        {
            "name": "disk.device.write.bytes",
            "type": "Cumulative",
            "unit": "B",
            "category": "disk",
            "description": "Volume of writes"
        },
        {
            "name": "disk.device.write.bytes.rate",
            "type": "Gauge",
            "unit": "B/s",
            "category": "disk",
            "description": "Average rate of writes"
        },
        {
            "name": "disk.write.requests",
            "type": "Cumulative",
            "unit": "request",
            "category": "compute",
            "description": "Number of write requests"
        },
        {
            "name": "disk.write.requests.rate",
            "type": "Gauge",
            "unit": "request/s",
            "category": "compute",
            "description": "Average rate of write requests"
        },
        {
            "name": "disk.read.bytes",
            "type": "Cumulative",
            "unit": "B",
            "category": "compute",
            "description": "Volume of reads"
        },
        {
            "name": "disk.read.bytes.rate",
            "type": "Gauge",
            "unit": "B/s",
            "category": "compute",
            "description": "Average rate of reads"
        },
        {
            "name": "disk.write.bytes",
            "type": "Cumulative",
            "unit": "B",
            "category": "compute",
            "description": "Volume of writes"
        },
        {
            "name": "disk.write.bytes.rate",
            "type": "Gauge",
            "unit": "B/s",
            "category": "compute",
            "description": "Average rate of writes"
        },
        {
            "name": "disk.read.requests",
            "type": "Cumulative",
            "unit": "request",
            "category": "compute",
            "description": "Number of read requests"
        },
        {
            "name": "disk.root.size",
            "type": "Gauge",
            "unit": "GB",
            "category": "compute",
            "description": "Size of root disk"
        },
        {
            "name": "disk.ephemeral.size",
            "type": "Gauge",
            "unit": "GB",
            "category": "compute",
            "description": "Size of ephemeral disk"
        }
    ]
}