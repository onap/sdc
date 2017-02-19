{
    "ceilometerInfoList": [
        {
            "name": "instance",
            "type": "Gauge",
            "unit": "instance",
            "category": "compute",
            "description": "Existence of instance"
        },
        {
            "name": "instance:type",
            "type": "Gauge",
            "unit": "instance",
            "category": "compute",
            "description": "Existence of instance <type> (OpenStack types)"
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
            "unit": "ms",
            "category": "compute",
            "description": "Average disk latency"
        }
    ]
}